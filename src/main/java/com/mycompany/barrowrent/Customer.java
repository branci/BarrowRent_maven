/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

import java.sql.Date;

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 */
public class Customer {
    
    private Long id;
    private String fullName;
    private java.sql.Date birthDate;
    private String idCard;
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
       
    public Date getBirthDate() {
        return birthDate;
    }
    
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
    
    public String getIdCard() {
        return idCard;
    }
    
    @Override
    public String toString() {
        return "Customer{" + id + '}';
    }
    
    //EQUALS a HASHCODE?
}
