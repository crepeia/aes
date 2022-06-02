/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Tip;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class TipDAO extends GenericDAO<Tip>{
    
    public TipDAO(EntityManager entityManager) throws NamingException {
        super(Tip.class);
        this.setEntityManager(entityManager);
    }
    
    
    
}
