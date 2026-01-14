/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Record;
import aes.model.User;
import aes.persistence.RecordDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@Path("secured/record")
@TransactionManagement(TransactionManagementType.BEAN)
public class RecordFacadeREST extends AbstractFacade<Record> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private RecordDAO recordDAO;
    private UserDAO userDAO;

    @Inject
    private SecurityContextHelper securityHelper;

    public RecordFacadeREST() {
        super(Record.class);
         try {
            recordDAO = new RecordDAO();
            userDAO = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRecord() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            recordDAO.create(loggedUser, em);

            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException ex) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /*
    @POST
    @Path("create/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(@PathParam("userId") Long userId) {
        Record entity;
        try {
            entity = recordDAO.create(userId, em);
            return Response.ok().entity(entity).build();
        } catch (SQLException ex) {
             Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();

        }
    }
    */
    
    @PUT
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(Record entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            if (entity == null) {
                Logger.getLogger(RecordFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_SIGNATURE reason=INVALID_DATA");
                return Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_DATA").build();
            }

            User loggedUser = securityHelper.getLoggedUser();

            Record oldRecord = recordDAO.find(entity.getId(), em);

            r = securityHelper.requireSameUser(oldRecord.getUser(), loggedUser.getId());
            if (r != null) return r;

            oldRecord.setDailyGoal(entity.getDailyGoal());
            oldRecord.setWeeklyGoal(entity.getWeeklyGoal());

            recordDAO.insertOrUpdate(oldRecord, em);
            return Response.ok().entity(oldRecord).build();
        } catch (SQLException ex) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("findByUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            Record rec = recordDAO.findByUserId(loggedUser.getId(), em);
            
            if (rec == null) {
                Logger.getLogger(RecordFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DID_NOT_FIND_RECORD reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            return Response.ok().entity(rec).build();
        } catch (Exception ex) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
       
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
