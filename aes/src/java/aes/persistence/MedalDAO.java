/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Medal;
import javax.naming.NamingException;

/**
 *
 * @author Matheus Carvalho
 */
public class MedalDAO extends GenericDAO<Medal> {
    
    public MedalDAO() throws NamingException {
        super(Medal.class);
    }
    
    
}
