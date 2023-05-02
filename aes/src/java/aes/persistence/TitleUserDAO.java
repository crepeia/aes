/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.TitleUser;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Matheus Carvalho
 */
public class TitleUserDAO extends GenericDAO<TitleUser> {
    
    public TitleUserDAO() throws NamingException {
        super(TitleUser.class);
    }
    
    public void insertMedalAndTitle(TitleUser titleUser,  EntityManager entityManager) throws SQLException {
        this.insertOrUpdate(titleUser, entityManager);
    }
}