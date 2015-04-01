/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

import com.mycompany.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 * @author Marek Perichta
 */
public class BarrowManagerImpl implements BarrowManager {
    

    final static Logger log = LoggerFactory.getLogger(BarrowManagerImpl.class);
    private final DataSource dataSource;
    
    public BarrowManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
        
    @Override
    public void createBarrow(Barrow barrow) throws ServiceFailureException {
        
        validate(barrow);


        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO BARROW (use,volumeLt) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, barrow.getUse());
                st.setDouble(2, barrow.getVolumeLt());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert barrow" + barrow);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                barrow.setId(getKey(keyRS, barrow));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all barrrows", ex);
            
        }
    }
        
    private Long getKey(ResultSet keyRS, Barrow barrow) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key "
                        + "retriving failed when trying to insert barrow " + barrow
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key "
                        + "retriving failed when trying to insert barrow " + barrow
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key "
                    + "retriving failed when trying to insert barrow " + barrow
                    + " - no key found");
        }
    }
    
    @Override
    public void updateBarrow(Barrow barrow) throws ServiceFailureException {
 
        validate(barrow);
        
        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("UPDATE BARROW SET use=?,volumeLt=? WHERE id=?")) {
                st.setString(1, barrow.getUse());
                st.setDouble(2, barrow.getVolumeLt());
                st.setLong(3,barrow.getId());
                if(st.executeUpdate()!=1) {
                    throw new IllegalArgumentException("cannot update barrow " + barrow);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all barrows", ex);
        }
        
    }

    @Override
    public void deleteBarrow(Barrow barrow) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM BARROW WHERE id=?")) {
                st.setLong(1, barrow.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete barrow with id =" + barrow.getId());
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all barrows", ex);
        }
    }
    
    @Override
    public List<Barrow> findAllBarrows() throws ServiceFailureException {
        log.debug("finding all barrows");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,use,volumeLt FROM BARROW")) {
                ResultSet rs = st.executeQuery();
                List<Barrow> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToBarrow(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all barrows", ex);
        }
    }
    
    private Barrow resultSetToBarrow(ResultSet rs) throws SQLException {
        Barrow barrow = new Barrow();
        barrow.setId(rs.getLong("id"));
        barrow.setUse(rs.getString("use"));
        barrow.setVolumeLt(rs.getDouble("volumeLt"));
        return barrow;
    }
    
    @Override
    public Barrow getBarrowById(Long id) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,use,volumeLt FROM BARROW WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Barrow barrow = resultSetToBarrow(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + barrow + " and " + resultSetToBarrow(rs));
                    }
                    return barrow;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all barrows", ex);
        }
    }
    
    static private void validate (Barrow barrow) {
        if (barrow == null) {
            throw new IllegalArgumentException("barrow is null");
        }
        if (barrow.getId() != null) {
            throw new IllegalArgumentException("barrow id is already set");
        }
        if (barrow.getUse() == null) {
            throw new IllegalArgumentException("barrow use is null");
        }   
        int compareVal = Double.compare(barrow.getVolumeLt(), 0D);
        
        if (compareVal == 0) {
            throw new IllegalArgumentException("barrow volumeLt is zero");
        }
        if (compareVal < 0) {
            throw new IllegalArgumentException("barrow volumeLt is negative number");
        }
    
    }
    
    
    
}
