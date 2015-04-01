/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

import com.mycompany.common.DBUtils;
import com.mycompany.common.ServiceFailureException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 */
public class BarrowManagerImplTest {
    
    private BarrowManagerImpl manager;    
    private DataSource ds;

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:derby:memory:barrowmgr-test;create=true");
        //ds.setUrl("jdbc:derby://localhost:1527/barrowmgr-test;create=true");
        return ds;
    }
    
    
    @Before
    public void setUp() throws SQLException, IOException {
        ds = prepareDataSource(); 
        manager = new BarrowManagerImpl(ds);
        DBUtils.executeSqlScript(ds,BarrowManagerImpl.class.getResourceAsStream("/createTables.sql"));
        
    }
    
    @After
    public void tearDown() throws SQLException, IOException {
        DBUtils.executeSqlScript(ds,BarrowManagerImpl.class.getResourceAsStream("/dropTables.sql"));
    }
    
    @Test
    public void createBarrow() {
        Barrow barrow = newBarrow("sand", 100.1D);
        manager.createBarrow(barrow);
        
        Long barrowId = barrow.getId();
        assertNotNull(barrowId);
        Barrow result = manager.getBarrowById(barrowId);
        assertEquals(barrow, result);
        assertNotSame(barrow, result);
        assertDeepEquals(barrow, result);
        
    }
    
    @Test
    public void getBarrow() {
        assertNull(manager.getBarrowById(1L));
        
        Barrow barrow = newBarrow("sand", 100.1D);
        manager.createBarrow(barrow);
        Long barrowId = barrow.getId();
        
        Barrow result = manager.getBarrowById(barrowId);
        assertEquals(barrow, result);
        assertDeepEquals(barrow, result);
    }
    
    @Test
    public void findAllBarrows() {
        assertTrue(manager.findAllBarrows().isEmpty());
        
        Barrow barrow1 = newBarrow("sand", 100.2D);
        Barrow barrow2 = newBarrow("soil", 200D);
        
        manager.createBarrow(barrow1);
        manager.createBarrow(barrow2);
        
        List<Barrow> expected = Arrays.asList(barrow1, barrow2);
        List<Barrow> actual = manager.findAllBarrows();
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        
        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }
    
//-----------------------------------------------CREATE WRONG BARROW-----------------------------------------------------------   
    @Test(expected = IllegalArgumentException.class)
    public void createNullBarrow() {
       
        manager.createBarrow(null);

    }
     
    @Test(expected = IllegalArgumentException.class)
    public void createBarrowWithWrongId() {
        
        Barrow barrow = newBarrow("soil", 200D);
        barrow.setId(1L);

        manager.createBarrow(barrow);

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createBarrowWithNullUse() {
        
        Barrow barrow = newBarrow(null, 200D);

        manager.createBarrow(barrow);

    }
     
    @Test(expected = IllegalArgumentException.class)
    public void createBarrowWithZeroVolume() {
        
        Barrow barrow = newBarrow("soil", 0D);

        manager.createBarrow(barrow);

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createBarrowWithNegativeVolume() {
        
        Barrow barrow = newBarrow("soil", -1D);

        manager.createBarrow(barrow);

    }
        
//------------------------------------------------------------------------------------------------------------------------------
 
    @Test
    public void updateBarrow() {
        Barrow barrow = newBarrow("sand", 100.2D);
        Barrow barrow2 = newBarrow("soil", 200D);
        manager.createBarrow(barrow);
        manager.createBarrow(barrow2);
        Long barrowId = barrow.getId();
        
        barrow = manager.getBarrowById(barrowId);
        barrow.setUse("water");
        manager.updateBarrow(barrow);
        assertEquals("water", barrow.getUse());
        assertEquals(100.2, barrow.getVolumeLt(), 0.01); //third value is a threshold within which two doubles should be considered "equal"
        
        barrow = manager.getBarrowById(barrowId);
        barrow.setVolumeLt(111.1D);
        manager.updateBarrow(barrow);
        assertEquals("water", barrow.getUse());
        assertEquals(111.1, barrow.getVolumeLt(), 0.01);
        
        /*
        barrow = manager.getBarrowById(barrowId);
        barrow.setUse(null);
        manager.updateBarrow(barrow);
        assertEquals(111.1, barrow.getVolumeLt(), 0.01);
        assertNull(barrow.getUse());
        */
        
        // Check if updates didn't affect other records
        assertDeepEquals(barrow2, manager.getBarrowById(barrow2.getId()));
    }
//----------------------------------------UPDATE WRONG BARROW-----------------------------------------
    @Test(expected = ServiceFailureException.class)
    public void updateBarrowWithNull() {
        Barrow barrow = newBarrow("sand", 100D);
        manager.createBarrow(barrow);
    
        manager.updateBarrow(null);

    }
    
    @Test(expected = ServiceFailureException.class)
    public void updateBarrowWithNullId() {
        Barrow barrow = newBarrow("sand", 100D);
        manager.createBarrow(barrow);
        Long barrowId = barrow.getId();
    
        barrow = manager.getBarrowById(barrowId);
        barrow.setId(null);
        manager.updateBarrow(barrow);        

    }
    
    @Test(expected = ServiceFailureException.class)
    public void updateBarrowWithWrongId() {
        Barrow barrow = newBarrow("sand", 100D);
        manager.createBarrow(barrow);
        Long barrowId = barrow.getId();
    
        barrow = manager.getBarrowById(barrowId);
        barrow.setId(barrowId - 1);
        manager.updateBarrow(barrow);           

    }
    
    @Test(expected = ServiceFailureException.class)
    public void updateBarrowWithNullUse() {
        Barrow barrow = newBarrow("sand", 100D);
        manager.createBarrow(barrow);
        Long barrowId = barrow.getId();
        
        barrow = manager.getBarrowById(barrowId);
        barrow.setUse(null);
        manager.updateBarrow(barrow);           

    }
    
    @Test(expected = ServiceFailureException.class)
    public void updateBarrowWithZeroVolume() {
        Barrow barrow = newBarrow("sand", 100D);
        manager.createBarrow(barrow);
        Long barrowId = barrow.getId();
        
        barrow = manager.getBarrowById(barrowId);
        barrow.setVolumeLt(0D);
        manager.updateBarrow(barrow);            

    }
    
    @Test(expected = ServiceFailureException.class)
    public void updateBarrowWithNegativeVolume(){
        Barrow barrow = newBarrow("sand", 100D);
        manager.createBarrow(barrow);
        Long barrowId = barrow.getId();
        
        barrow = manager.getBarrowById(barrowId);
        barrow.setVolumeLt(-1D);
        manager.updateBarrow(barrow);          

    }
               
//------------------------------------------------------------------------------------------------------

    @Test 
    public void deleteBarrow() {
        Barrow barr1 = newBarrow("sand", 100D);
        Barrow barr2 = newBarrow("soil", 200D);
        
        manager.createBarrow(barr1);
        manager.createBarrow(barr2);
        
        assertNotNull(manager.getBarrowById(barr1.getId()));
        assertNotNull(manager.getBarrowById(barr2.getId()));
        
        manager.deleteBarrow(barr1);
        
        assertNull(manager.getBarrowById(barr1.getId()));
        assertNotNull(manager.getBarrowById(barr2.getId()));
    }
//----------------------------------DELETE WRONG BARROW-------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void deleteNullBarrow() {
        
        manager.deleteBarrow(null);

    }
    
    @Test(expected = NullPointerException.class)
    public void deleteBarrowWithNullId() {
        Barrow barrow = newBarrow("sand", 100D);
        
        barrow.setId(null);
        manager.deleteBarrow(barrow);

    }
    
    @Test(expected = ServiceFailureException.class)
    public void deleteBarrowWithWrongId() {
        Barrow barrow = newBarrow("sand", 100D);
        
        barrow.setId(1l);
        manager.deleteBarrow(barrow);

    }
//------------------------------------------------------------------------------------------------------
    private Barrow newBarrow(String use, Double volumeLt) {
        Barrow barrow = new Barrow();
        barrow.setUse(use);
        barrow.setVolumeLt(volumeLt);
        
        return barrow;
    }
    
    private void assertDeepEquals(Barrow expected, Barrow actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUse(), actual.getUse());
        assertEquals(expected.getVolumeLt(), actual.getVolumeLt());
    }
    
    private void assertDeepEquals(List<Barrow> expectedList, List<Barrow> actualList) {
        for(int i = 0; i < expectedList.size(); i++) {
            Barrow expected = expectedList.get(i);
            Barrow actual = actualList.get(i);
            assertEquals(expected, actual);
        }
    }
    
    private static final Comparator<Barrow> idComparator = new Comparator<Barrow>() {
        
        @Override
        public int compare(Barrow o1, Barrow o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
    
}
