/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

import com.mycompany.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 */
public class LeaseManagerImpl implements LeaseManager {
    
    final static Logger log = LoggerFactory.getLogger(LeaseManagerImpl.class);
    private final DataSource dataSource;
    
    public LeaseManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    
    @Override
    public void createLease(Lease lease) throws ServiceFailureException {
                
        validate(lease);

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO LEASE "
                    + "(customerId,barrowId,price,realEndTime,startTime,expectedEndTime) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setLong(1, lease.getCustomer().getId());
                st.setLong(2, lease.getBarrow().getId());
                st.setBigDecimal(3, lease.getPrice());
                st.setDate(4, (Date) lease.getRealEndTime());
                st.setDate(5, (Date) lease.getStartTime());
                st.setDate(6, (Date) lease.getExpectedEndTime());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert lease " + lease);
                }
                ResultSet keyRS = st.getResultSet();
                lease.setId(getKey(keyRS, lease));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }
    
    
    private Long getKey(ResultSet keyRS, Lease lease) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key "
                        + "retriving failed when trying to insert lease " + lease
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key "
                        + "retriving failed when trying to insert lease " + lease
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key "
                    + "retriving failed when trying to insert lease " + lease
                    + " - no key found");
        }
    }
    
    
    
    @Override
    public void updateLease(Lease lease) throws ServiceFailureException {
        validate(lease);
        
        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("UPDATE LEASE SET customerId=?,barrowId=?,price=?,realEndTime=?,startTime=?,expectedEndTime=? WHERE id=?")) {
                st.setLong(1, lease.getCustomer().getId());
                st.setLong(2, lease.getBarrow().getId());
                st.setBigDecimal(3, lease.getPrice());
                st.setDate(4, (Date) lease.getRealEndTime());
                st.setDate(5, (Date) lease.getStartTime());
                st.setDate(6, (Date) lease.getExpectedEndTime());
                st.setLong(7, lease.getId());
                if(st.executeUpdate()!=1) {
                    throw new IllegalArgumentException("cannot update lease " + lease);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
        
    }
    
    @Override
    public void deleteLease(Lease lease) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM LEASE WHERE id=?")) {
                
                st.setLong(1, lease.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete lease with id =" + lease.getId());
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }
    
    @Override
    public List<Lease> findAllLeases() throws ServiceFailureException {
        log.debug("finding all leases");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,customerId,"
                    + "barrowId,price,realEndTime,startTime,expectedEndTime FROM LEASE")) {
                ResultSet rs = st.executeQuery();
                List<Lease> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToLease(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }
             
    @Override
    public List<Lease> findLeasesForCustomer(Customer customer) throws ServiceFailureException {
        log.debug("finding all leases for customer");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,customerId,"
                    + "barrowId,price,realEndTime,startTime,expectedEndTime FROM LEASE WHERE customerId=?")) {
                st.setLong(1, customer.getId());
                ResultSet rs = st.executeQuery();
                List<Lease> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToLease(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }

    @Override
    public List<Lease> findLeasesForBarrow(Barrow barrow) throws ServiceFailureException {
                log.debug("finding all leases for barrow");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,customerId,"
                    + "barrowId,price,realEndTime,startTime,expectedEndTime FROM LEASE WHERE barrowId=?")) {
                st.setLong(1, barrow.getId());
                ResultSet rs = st.executeQuery();
                List<Lease> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToLease(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }
    
    @Override
    public Lease getLeaseById(Long id) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,customerId,"
                    + "barrowId,price,realEndTime,startTime,expectedEndTime FROM LEASE WHERE id=?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Lease lease = resultSetToLease(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                + "(source id: " + id + ", found " + lease + " and " + resultSetToLease(rs));
                    }
                    return lease;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }
    
    private Lease resultSetToLease(ResultSet rs) throws SQLException {
        Lease lease = new Lease();
        lease.setId(rs.getLong("id"));
        lease.setCustomer(resultSetToCustomer(rs));
        lease.setBarrow(resultSetToBarrow(rs)); 
        lease.setPrice(rs.getBigDecimal("price"));
        lease.setRealEndTime(rs.getDate("realEndTime"));
        lease.setStartTime(rs.getDate("startTime")); 
        lease.setExpectedEndTime(rs.getDate("expectedEndTime")); 
        return lease;
    }
    
    private Customer resultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setFullName(rs.getString("fullName"));
        customer.setBirthDate(rs.getDate("birthDate"));
        customer.setIdCard(rs.getString("idCard"));
        return customer;
    }
    
    private Barrow resultSetToBarrow(ResultSet rs) throws SQLException {
        Barrow barrow = new Barrow();
        barrow.setId(rs.getLong("id"));
        barrow.setUse(rs.getString("use"));
        barrow.setVolumeLt(rs.getDouble("volumeLt"));
        return barrow;
    }
    
    private static void validate(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease is null");
        }
        if (lease.getId() != null) {
            throw new IllegalArgumentException("lease id is already set");
        }
        if (lease.getCustomer() == null) {
            throw new IllegalArgumentException("customer in lease is null");
        }
        if (lease.getBarrow() == null) {
            throw new IllegalArgumentException("barrow in lease is null");
        }
        if (lease.getPrice().signum() < 0) {
            throw new IllegalArgumentException("price is negative number");
        }
        if (lease.getStartTime().after(lease.getExpectedEndTime())) {
            throw new IllegalArgumentException("startTime is after expectedEndTime");
        }
        if (lease.getStartTime().after(lease.getRealEndTime())) {
            throw new IllegalArgumentException("startTime is after realEndTime");
        }
    }
    
}
