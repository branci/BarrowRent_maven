/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.barrowrent;

/**
 *
 * @author Branislav Smik <xsmik @fi.muni>
 */
public class Barrow {
    
    private Long id;
    private String use;
    private Double volumeLt;
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setUse(String use) {
        this.use = use;
    }
    
    public String getUse() {
        return use;
    }
    
    public void setVolumeLt(Double volumeLt) {
        this.volumeLt = volumeLt;
    }
    
    public Double getVolumeLt() {
        return volumeLt;
    }
    
    @Override
    public String toString() {
        return "Barrow{" + id + '}';
    }
    
    

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    
}
