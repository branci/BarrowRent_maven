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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Barrow other = (Barrow) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    } 

    
    
    
}
