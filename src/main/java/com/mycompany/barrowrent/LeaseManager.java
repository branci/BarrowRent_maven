/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

import com.mycompany.common.ServiceFailureException;
import java.util.List;

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 */
public interface LeaseManager {
    
    /**
     *
     * @param lease
     * @throws ServiceFailureException
     */
    void createLease(Lease lease) throws ServiceFailureException;
    
    /**
     *
     * @param lease
     * @throws ServiceFailureException
     */
    void updateLease(Lease lease) throws ServiceFailureException;
    
    /**
     *
     * @param leaseId 
     * @throws ServiceFailureException
     */
    void deleteLease(Lease lease) throws ServiceFailureException;
    
    /**
     *
     * @return
     * @throws ServiceFailureException
     */
    List<Lease> findAllLeases() throws ServiceFailureException;
    
    /**
     *
     * @param customer
     * @return
     * @throws ServiceFailureException
     */
    List<Lease> findLeasesForCustomer(Customer customer) throws ServiceFailureException;
    
    /**
     *
     * @param barrow
     * @return
     * @throws ServiceFailureException
     */
    List<Lease> findLeasesForBarrow(Barrow barrow) throws ServiceFailureException;
    
    /**
     *
     * @param id
     * @return
     * @throws ServiceFailureException
     */
    Lease getLeaseById(Long id) throws ServiceFailureException;
    
}
