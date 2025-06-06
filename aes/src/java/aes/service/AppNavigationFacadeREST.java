/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AppNavigation;
import aes.persistence.AppNavigationDAO;
import aes.persistence.GenericDAO;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Leonorico
 */
@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("appnavigation")
public class AppNavigationFacadeREST extends AbstractFacade<AppNavigation> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AppNavigationDAO appNavigationDao;

    public AppNavigationFacadeREST() {
        super(AppNavigation.class);
        try {
            appNavigationDao = new AppNavigationDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AppNavigationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @Path("saveNavigation")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveNavigation(AppNavigation appNavigation) {
        try {
            appNavigationDao.saveNavigation(appNavigation, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(AppNavigationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
