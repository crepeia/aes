/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AppSuggestion;
import aes.model.User;
import aes.persistence.AppSuggestionDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("secured/appsuggestion")
@Secured
public class AppSuggestionFacadeREST extends AbstractFacade<AppSuggestion> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AppSuggestionDAO appSuggestionDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public AppSuggestionFacadeREST() {
        super(AppSuggestion.class);
        try {
            appSuggestionDao = new AppSuggestionDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AppSuggestionFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }

    @POST
    @Path("create")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createAppSuggestion(AppSuggestion entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity.getUser() != null) {
                r = securityHelper.requireSameUser(loggedUser, entity.getUser().getId());
                if (r != null) return r;
            }
            
            entity.setUser(loggedUser);
            
            appSuggestionDao.insert(entity, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AppSuggestionFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    /*
    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Long id, AppSuggestion entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }
    */

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
