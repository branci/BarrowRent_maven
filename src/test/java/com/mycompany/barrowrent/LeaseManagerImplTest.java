/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

import com.mycompany.common.DBUtils;
import com.mycompany.common.ServiceFailureException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.sql.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;



//Treba popridavat v testoch do managerov barrowy & customerov (pridat testy s barrowwithnullid...)

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 * @author Marek Perichta
 */
public class LeaseManagerImplTest {

    private LeaseManagerImpl manager;
    private CustomerManagerImpl customerManager;
    private BarrowManagerImpl barrowManager;
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private DataSource ds;
    
    
    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        //we will use in memory database
        ds.setUrl("jdbc:derby:memory:barrowmgr-test;create=true");
        //ds.setUrl("jdbc:derby://localhost:1527/barrowmgr-test");
        return ds;
    }
    
    
    private Barrow b1, b2, b3, barrowWithNullId, barrowNotIdDatabase;
    private Customer c1, c2, c3, customerWithNullId, customerNotInDatabase;
    
    private void prepareTestData() {
        
        b1 = newBarrow("sand", 100D);
        b2 = newBarrow("soil", 200D);
        b3 = newBarrow("muck", 300D);
        
        c1 = newCustomer("John Kviatkowsky",setDate("1986-01-22"),"0976SK41");
        c2 = newCustomer("Jozo Petrik",setDate("1991-04-20"),"0936SK00");
        c3 = newCustomer("Jozko Mrkvicka", setDate("1986-01-22"), "0976SK25");
        
        barrowManager.createBarrow(b1);
        barrowManager.createBarrow(b2);
        barrowManager.createBarrow(b3);
        
        customerManager.createCustomer(c1);
        customerManager.createCustomer(c2);
        customerManager.createCustomer(c3);
        
        barrowWithNullId = newBarrow("stones", 50D);
        barrowNotIdDatabase = newBarrow("stones", 50D);
        barrowNotIdDatabase.setId(b3.getId() + 100);
        customerWithNullId = newCustomer(null, null, null);
        customerNotInDatabase = newCustomer("Filip Modry", setDate("1986-01-22"), "0976SK25");
        customerNotInDatabase.setId(c3.getId() + 100);
    }
    
    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, LeaseManagerImpl.class.getResourceAsStream("/createTables.sql"));
        manager = new LeaseManagerImpl(ds);
        customerManager = new CustomerManagerImpl(ds);
        barrowManager = new BarrowManagerImpl(ds);
        //prepareTestData();
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds, LeaseManagerImpl.class.getResourceAsStream("/dropTables.sql"));
    }
    
    
    @Test
    public void createLease() {
        BigDecimal price = new BigDecimal("1000.99");
        Lease lease = newLease(c1, b1, price, setDate("2015-08-22"), setDate("2015-08-22"), setDate("2015-08-22"));

        manager.createLease(lease);

        Long leaseId = lease.getId();
        assertNotNull(leaseId);
        Lease result = manager.getLeaseById(leaseId);
        assertEquals(lease, result);
        assertNotSame(lease, result);
        assertDeepEquals(lease, result);
    }

    @Test
    public void getLease() {
        assertNull(manager.getLeaseById(1L));

        BigDecimal price = new BigDecimal("1000.99");
        Lease lease = newLease(c1, b1, price, setDate("2015-08-22"), setDate("2015-08-22"), setDate("2015-08-22"));

        manager.createLease(lease);
        Long leaseId = lease.getId();

        Lease result = manager.getLeaseById(leaseId);
        assertEquals(lease, result);
        assertDeepEquals(lease, result);
    }
    
//----------------------------------------UPDATE GOOD LEASE----------------------------------------------------------    
    @Test
    public void updateLeaseBarrow() {
        
        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1983-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = lease1.getId();
        
        lease1 = manager.getLeaseById(leaseId);
        lease1.setBarrow(b3);
        manager.updateLease(lease1);
        assertEquals(b3, lease1.getBarrow());
        assertEquals(c1, lease1.getCustomer());
        assertEquals(new BigDecimal(1.0), lease1.getPrice());
        assertEquals(setDate("1983-01-22"), lease1.getRealEndTime());
        assertEquals(setDate("1981-01-22"), lease1.getStartTime());
        assertEquals(setDate("1982-01-22"), lease1.getExpectedEndTime());
        
        // Check if updates didn't affect other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));  
    }
    
    @Test
    public void updateLeaseCustomer() {

        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1983-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = lease1.getId();
        
        lease1 = manager.getLeaseById(leaseId);
        lease1.setCustomer(c2);
        manager.updateLease(lease1);
        assertEquals(b1, lease1.getBarrow());
        assertEquals(c2, lease1.getCustomer());
        assertEquals(new BigDecimal(1.0), lease1.getPrice());
        assertEquals(setDate("1983-01-22"), lease1.getRealEndTime());
        assertEquals(setDate("1981-01-22"), lease1.getStartTime());
        assertEquals(setDate("1982-01-22"), lease1.getExpectedEndTime());
        
        // Check if updates didn't affect other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));  
    }
    
    @Test
    public void updateLeasePrice() {

        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1983-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = lease1.getId();
        
        lease1 = manager.getLeaseById(leaseId);
        lease1.setPrice(new BigDecimal(12.34));
        manager.updateLease(lease1);
        assertEquals(b1, lease1.getBarrow());
        assertEquals(c1, lease1.getCustomer());
        assertEquals(new BigDecimal(12.34), lease1.getPrice());
        assertEquals(setDate("1983-01-22"), lease1.getRealEndTime());
        assertEquals(setDate("1981-01-22"), lease1.getStartTime());
        assertEquals(setDate("1982-01-22"), lease1.getExpectedEndTime());
        
        // Check if updates didn't affect other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));  
    }
    
    @Test
    public void updateLeaseRealEndTime() {

        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1983-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = lease1.getId();
        
        lease1 = manager.getLeaseById(leaseId);
        lease1.setRealEndTime(setDate("1983-01-25"));
        manager.updateLease(lease1);
        assertEquals(b1, lease1.getBarrow());
        assertEquals(c1, lease1.getCustomer());
        assertEquals(new BigDecimal(1.0), lease1.getPrice());
        assertEquals(setDate("1983-01-25"), lease1.getRealEndTime()); //25 is different
        assertEquals(setDate("1981-01-22"), lease1.getStartTime());
        assertEquals(setDate("1982-01-22"), lease1.getExpectedEndTime());
        
        // Check if updates didn't affect other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));  
    }
    
    @Test
    public void updateLeaseStartTime() {

        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1983-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = lease1.getId();
        
        lease1 = manager.getLeaseById(leaseId);
        lease1.setStartTime(setDate("1983-01-25"));
        manager.updateLease(lease1);
        assertEquals(b1, lease1.getBarrow());
        assertEquals(c1, lease1.getCustomer());
        assertEquals(new BigDecimal(1.0), lease1.getPrice());
        assertEquals(setDate("1983-01-22"), lease1.getRealEndTime());
        assertEquals(setDate("1983-01-22"), lease1.getStartTime());     //25 is different
        assertEquals(setDate("1982-01-22"), lease1.getExpectedEndTime());
        
        // Check if updates didn't affect other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));  
    }
    
    
    @Test
    public void updateExpectedEndTime() {

        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1983-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = lease1.getId();
        
        lease1 = manager.getLeaseById(leaseId);
        lease1.setExpectedEndTime(setDate("1983-01-25"));
        manager.updateLease(lease1);
        assertEquals(b1, lease1.getBarrow());
        assertEquals(c1, lease1.getCustomer());
        assertEquals(new BigDecimal(1.0), lease1.getPrice());
        assertEquals(setDate("1983-01-22"), lease1.getRealEndTime());
        assertEquals(setDate("1981-01-22"), lease1.getStartTime());     
        assertEquals(setDate("1983-01-25"), lease1.getExpectedEndTime()); //25 is different
        
        // Check if updates didn't affect other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));  
    }
//---------------------------------------------------------------------------------------------------    

    @Test
    public void deleteLease() {

        Lease lease1 = newLease(c1, b1, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        Lease lease2 = newLease(c1, b2, new BigDecimal(1.0), 
                setDate("1985-01-22"), setDate("1981-01-22"), setDate("1982-01-22"));
        
        manager.createLease(lease1);
        manager.createLease(lease2);
        
        assertNotNull(manager.getLeaseById(lease1.getId()));
        assertNotNull(manager.getLeaseById(lease2.getId()));
       
        manager.deleteLease(lease1);
        
        assertNull(manager.getLeaseById(lease1.getId()));
        assertNotNull(manager.getLeaseById(lease2.getId()));
    }
//----------------------------------------------DELETE WRONG LEASE-----------------------------------------    
    @Test(expected = NullPointerException.class)
    public void deleteNullLease() {
        
        manager.deleteLease(null);

    } 
    
    @Test(expected = NullPointerException.class)
    public void deleteLeaseWithNullId() {
        Lease lease = newLease(c1, b1, new BigDecimal("1000.99"), 
                setDate("2018-08-22"), setDate("2015-08-22"), setDate("2016-08-22"));
        
        lease.setId(null);
        manager.deleteLease(lease);

    }       
    
    @Test(expected = ServiceFailureException.class)
    public void deleteLeaseWithWrongId() {
        Lease lease = newLease(c1, b1, new BigDecimal("1000.99"), 
                setDate("2018-08-22"), setDate("2015-08-22"), setDate("2016-08-22"));
        
        lease.setId(1L);
        manager.deleteLease(lease);

    } 
    
//----------------------------------CREATE WRONG LEASE-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void createNullLease() {
        
        manager.createLease(null);

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createLeaseWithWrongId() {
        Lease lease = newLease(c1, b1, new BigDecimal("1000.99"), 
                setDate("2018-08-22"), setDate("2015-08-22"), setDate("2016-08-22"));
        
        lease.setId(1L);
        
        manager.createLease(lease);

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createLeaseWithZeroPrice() {
        Lease lease = newLease(c1, b1, new BigDecimal("0"), 
                setDate("2018-08-22"), setDate("2015-08-22"), setDate("2016-08-22"));
        
        manager.createLease(lease);

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createLeaseWithNegativePrice() {
        Lease lease = newLease(c1, b1, new BigDecimal("-1"), 
                setDate("2018-08-22"), setDate("2015-08-22"), setDate("2016-08-22"));
        
        manager.createLease(lease);
            
    }
    

    //tests if startTime isn't after expectedReturnTime
    @Test(expected = IllegalArgumentException.class)
    public void createLeaseWithWrongExpectedTime() {
        Lease lease = newLease(c1, b1, new BigDecimal("100"), 
                setDate("2016-08-22"), setDate("2015-08-22"), setDate("2014-08-22"));
        
        manager.createLease(lease);

    }
    
    //tests if startTime isn't after realEndTime
    @Test(expected = IllegalArgumentException.class)
    public void createLeaseWithWrongRealTime() {
        Lease lease = newLease(c1, b1, new BigDecimal("100"), 
                setDate("2014-08-22"), setDate("2015-08-22"), setDate("2016-08-22"));
        
        manager.createLease(lease);

    }
    
//---------------------------------------------------------------------------------------------------------
    
    
            
//-----MAREK
     @Test
    public void getAllLeasesForCustomer() {
   
        assertTrue(manager.findLeasesForCustomer(c1).isEmpty());
                
        Lease l1 = newLease(c1,b1, new BigDecimal(1.0), setDate("1983-01-22"),setDate("1981-01-22"),setDate("1982-01-22"));
        Lease l2 = newLease(c1,b2, new BigDecimal(1.0), setDate("1985-01-22"),setDate("1981-01-22"),setDate("1982-01-22"));
        Lease l3 = newLease(c2,b3, new BigDecimal(1.0), setDate("1984-01-22"),setDate("1982-01-22"),setDate("1983-01-22"));
        
        manager.createLease(l1);
        manager.createLease(l2);
        manager.createLease(l3);
        
        List<Lease> expected = Arrays.asList(l1,l2);
        List<Lease> actual = manager.findLeasesForCustomer(c1);
        
        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
        
    }
    
    
    @Test
    public void getAllLeasesForBarrow() {
              
        assertTrue(manager.findLeasesForBarrow(b1).isEmpty());
        
        Lease l1 = newLease(c1,b1, new BigDecimal(1.0), setDate("1983-01-22"),setDate("1981-01-22"),setDate("1982-01-22"));
        Lease l2 = newLease(c2,b1, new BigDecimal(1.0), setDate("1985-01-22"),setDate("1981-01-22"),setDate("1982-01-22"));
        Lease l3 = newLease(c3,b2, new BigDecimal(1.0), setDate("1984-01-22"),setDate("1980-01-22"),setDate("1983-01-22"));
        
        manager.createLease(l1);
        manager.createLease(l2);
        manager.createLease(l3);
        
        List<Lease> expected = Arrays.asList(l1, l2);
        List<Lease> actual = manager.findLeasesForBarrow(b1);
        
        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
        
    }
    
    @Test
    public void getAllLeases() {
           
        assertTrue(manager.findLeasesForCustomer(c1).isEmpty());
        
        Lease l1 = newLease(c1,b1, new BigDecimal(1.0), setDate("1983-01-22"),setDate("1981-01-22"),setDate("1982-01-22"));
        Lease l2 = newLease(c2,b1, new BigDecimal(1.0), setDate("1985-01-22"),setDate("1981-01-22"),setDate("1982-01-22"));
        
        manager.createLease(l1);
        manager.createLease(l2);
        
        List<Lease> expected = Arrays.asList(l1, l2);
        List<Lease> actual = manager.findAllLeases();
        
        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
        
    }

//-----MAREK koniec
    
//    ----------------------------------------------------------------------------------------------------------------

    private static Lease newLease(Customer customer, Barrow barrow, BigDecimal price,
            Date realEndTime, Date startTime, Date expectedEndTime) {
        Lease lease = new Lease();
        lease.setPrice(price);
        lease.setRealEndTime(realEndTime);
        lease.setExpectedEndTime(expectedEndTime);
        lease.setStartTime(startTime);

        return lease;

    }

    private Barrow newBarrow(String use, Double volumeLt) {
        Barrow barrow = new Barrow();
        barrow.setUse(use);
        barrow.setVolumeLt(volumeLt);

        return barrow;
    }

    private static Customer newCustomer(String fullName, Date birthDate, String idCard) {
        Customer customer = new Customer();
        customer.setBirthDate(birthDate);
        customer.setFullName(fullName);
        customer.setIdCard(idCard);

        return customer;
    }

    private void assertDeepEquals(Lease expected, Lease actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCustomer(), actual.getCustomer());
        assertEquals(expected.getBarrow(), actual.getBarrow());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getRealEndTime(), actual.getRealEndTime());
        assertEquals(expected.getStartTime(), actual.getStartTime());
        assertEquals(expected.getExpectedEndTime(), actual.getExpectedEndTime());

    }
    
    private void assertDeepEquals(List<Lease> expectedList, List<Lease> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Lease expected = expectedList.get(i);
            Lease actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private java.sql.Date setDate(String date) {
        return Date.valueOf(date);
    }
       /*
    private Date setDate(String dateInString) {
        try {
            Date date = sdf.parse(dateInString);
            return date;
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    */
}