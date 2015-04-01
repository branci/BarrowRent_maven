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
 * @author Marek Perichta
 */
public class CustomerManagerImpl implements CustomerManager {
    
    final static Logger log = LoggerFactory.getLogger(CustomerManagerImpl.class);
    private final DataSource dataSource;

    public CustomerManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source is not set");
        }
    }
    
    @Override
    public void createCustomer(Customer customer) throws ServiceFailureException {
        checkDataSource();
        validate(customer);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO CUSTOMER (fullName,birthDate,idCard) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, customer.getFullName());
                st.setDate(2, (Date) customer.getBirthDate());
                st.setString(3, customer.getIdCard());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert customer " + customer);
                }
                ResultSet keyRS = st.getResultSet();
                customer.setId(getKey(keyRS, customer));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
    }
    
    private Long getKey(ResultSet keyRS, Customer customer) throws ServiceFailureException, SQLException {
        //test keyRS
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key "
                        + "retriving failed when trying to insert customer " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key "
                        + "retriving failed when trying to insert customer " + customer
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key "
                    + "retriving failed when trying to insert customer " + customer
                    + " - no key found");
        }
    }
    
    @Override
    public void updateCustomer(Customer customer) throws ServiceFailureException {
        checkDataSource();
        validate(customer);
        
        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("UPDATE CUSTOMER SET fullName=?,birthDate=?,idCard=? WHERE id=?")) {
                st.setString(1, customer.getFullName());
                st.setDate(2, (Date) customer.getBirthDate());
                st.setString(3, customer.getIdCard());
                st.setLong(4,customer.getId());   
                if(st.executeUpdate()!=1) {
                    throw new IllegalArgumentException("cannot update customer " + customer);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
        
        
    }
    
    @Override
    public void deleteCustomer(Customer customer) throws ServiceFailureException {
        validate(customer);
        checkDataSource();
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM CUSTOMER WHERE id=?")) {
                st.setLong(1, customer.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete customer with id =" + customer.getId());
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
    }
    
    @Override
    public Customer getCustomerById(Long id) throws ServiceFailureException {
        checkDataSource();
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,fullName,birthDate,idCard FROM CUSTOMER WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Customer customer = resultSetToCustomer(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + customer + " and " + resultSetToCustomer(rs));
                    }
                    return customer;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
    }
    
    @Override
    public List<Customer> findAllCustomers() throws ServiceFailureException {
        checkDataSource();
        
        log.debug("finding all customers");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,fullName,birthDate,idCard FROM CUSTOMER")) {
                ResultSet rs = st.executeQuery();
                List<Customer> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToCustomer(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
    }
    
    private Customer resultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setFullName(rs.getString("fullName"));
        customer.setBirthDate(rs.getDate("birthDate"));
        customer.setIdCard(rs.getString("idCard"));
        return customer;
    }
    
    private static void validate (Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer is null");
        }
        if (customer.getId() != null) {
            throw new IllegalArgumentException("customer id is already set");
        }
        if (customer.getFullName() == null) {
            throw new IllegalArgumentException("customer name is null");
        }
        if (customer.getIdCard() == null) {
            throw new IllegalArgumentException("customer id card number is null");
        }
        if (customer.getBirthDate() == null) {
            throw new IllegalArgumentException("customer birth date is null");
        }
    }
    
}
