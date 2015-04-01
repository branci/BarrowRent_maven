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
public interface CustomerManager {
    
    /**
     *
     * @param customer
     * @throws ServiceFailureException
     */
    void createCustomer(Customer customer) throws ServiceFailureException;
    
    /**
     *
     * @param customer
     * @throws ServiceFailureException
     */
    void updateCustomer(Customer customer) throws ServiceFailureException;
    
    /**
     *
     * @param customerId
     * @throws ServiceFailureException
     */
    void deleteCustomer(Customer customer) throws ServiceFailureException;
    
    /**
     *
     * @param id
     * @return
     * @throws ServiceFailureException
     */
    Customer getCustomerById(Long id) throws ServiceFailureException;
    
    /**
     *
     * @return
     * @throws ServiceFailureException
     */
    List<Customer> findAllCustomers() throws ServiceFailureException;
    
}
