/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.MedalUser;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Matheus Carvalho
 */
public class MedalUserDAO extends GenericDAO<MedalUser> {
    
    public MedalUserDAO() throws NamingException {
        super(MedalUser.class);
    }
    
    public void insertMedalAndTitle(MedalUser medalUser, EntityManager entityManager) throws SQLException {
        this.insertOrUpdate(medalUser, entityManager);    
    }
    
     
}
