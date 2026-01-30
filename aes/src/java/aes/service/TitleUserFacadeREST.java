/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Title;
import aes.model.TitleUser;
import aes.model.User;
import aes.persistence.TitleDAO;
import aes.persistence.TitleUserDAO;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author LEVTY
 */
@Stateless
@Path("titleuser")
@Secured
@TransactionManagement(TransactionManagementType.BEAN)
public class TitleUserFacadeREST extends AbstractFacade<TitleUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private TitleDAO titleDao;
    private TitleUserDAO titleUserDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public TitleUserFacadeREST() {
        super(TitleUser.class);
        try {
            titleUserDao = new TitleUserDAO();
            titleDao = new TitleDAO();
        } catch (NamingException ex) {
            Logger.getLogger(TitleUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTitle(TitleUser entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            if (entity == null || entity.getTitle() == null || entity.getDescription() == null) {
                Logger.getLogger(TitleUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_SIGNATURE reason=INVALID_DATA");
                
                return Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_DATA").build();
            }
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Title title = titleDao.find(entity.getTitle().getId(), em);
            
            if (title == null) {
                Logger.getLogger(TitleUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_SIGNATURE reason=INVALID_DATA");
                
                return Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_DATA").build();
            }
            
            List<TitleUser> teList = titleUserDao.findByUserTitleDescription(loggedUser.getId(), title.getId(), entity.getDescription(), em);
            
            if (teList.isEmpty()) {
                titleUserDao.insert(entity, em);
                return Response.status(Response.Status.CREATED).build();
            } else {
                Logger.getLogger(TitleUserFacadeREST.class.getName())
                    .log(Level.INFO, "title list already exists");
                
                return Response.ok().build();
            }
        } catch (SQLException ex) {
            Logger.getLogger(TitleUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
     
    @GET
    @Path("findByUser")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByUser() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<TitleUser> list = titleUserDao.findByUser(loggedUser.getId(), em);
            
            return Response.ok().entity(list).build();
        } catch (Exception ex) {
            Logger.getLogger(TitleUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("findAll")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllTitle() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            List<TitleUser> list = titleUserDao.findAll(em);
            
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TitleUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
