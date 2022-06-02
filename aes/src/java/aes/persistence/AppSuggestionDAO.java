/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AppSuggestion;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class AppSuggestionDAO extends GenericDAO<AppSuggestion>{
    
    public AppSuggestionDAO(EntityManager entityManager) throws NamingException {
        super(AppSuggestion.class);
        this.setEntityManager(entityManager);
    }
    
    
    public AppSuggestion createAppSuggestion(AppSuggestion entity) throws SQLException {
        super.insert(entity);
        return entity;
    }
    
}
