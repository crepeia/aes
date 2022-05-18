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
import javax.ws.rs.core.Response;

/**
 *
 * @author patri
 */
public class AppSuggestionDAO extends GenericDAO<AppSuggestion>{
    
    public AppSuggestionDAO() throws NamingException {
        super(AppSuggestion.class);
    }
    
    
    public AppSuggestion createAppSuggestion(AppSuggestion entity, EntityManager entityManager) throws SQLException {
        super.insert(entity, entityManager);
        return entity;
    }
    
}
