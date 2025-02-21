/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AppNavigation;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Leonorico
 */
public class AppNavigationDAO extends GenericDAO<AppNavigation> {
    
    public AppNavigationDAO() throws NamingException {
        super(AppNavigation.class);
    }
    
    public void saveNavigation(AppNavigation entity, EntityManager em) throws SQLException {
        //Aqui serão inseridas as tratativas da entidade recebida, provavelmente. Caso não,
        //deletar essa função e usar insertOrUpdate no appNavigationFacadeREST
        super.insertOrUpdate(entity, em);
    }
}
