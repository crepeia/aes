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

    @Context
    SecurityContext securityContext;

    public RecordFacadeREST() {
        super(Record.class);
         try {
            recordDAO = new RecordDAO(em);
            userDAO = new UserDAO(em);
        } catch (NamingException ex) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRecord(Record entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        //User u = em.find(User.class, entity.getUser().getId());
        User u = userDAO.find(entity.getUser().getId());
        if (!u.getEmail().equals(userEmail)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            //Record created = super.create(entity);
            Record created = recordDAO.create(u.getId());

            return Response.ok().entity(created).build();
        } catch (SQLException e) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("create/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(@PathParam("userId") Long userId) {
        //String userEmail = securityContext.getUserPrincipal().getName();

        
        Record entity;
        try {
            entity = recordDAO.create(userId);
            return Response.ok().entity(entity).build();
        } catch (SQLException ex) {
             Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();

        }
        
        
        /*try {
            Record entity = new Record();
            entity.setUser(em.find(User.class, userId));
            entity.setDailyGoal(0);
            entity.setWeeklyGoal(0);
            super.create(entity);
            return Response.ok().entity(entity).build();
        } catch (Exception e) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }*/

    }

    @PUT
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Record edit(Record entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        //User u = em.find(User.class, entity.getUser().getId());
        User u = userDAO.find(entity.getUser().getId());

        if (u.getEmail().equals(userEmail)) {
            try {
                //super.edit(entity);
                recordDAO.insertOrUpdate(entity);
            } catch (SQLException ex) {
                Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return entity;
    }

    @GET
    @Path("find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userId") Long userId) {
        String userEmail = securityContext.getUserPrincipal().getName();
        //User u = em.find(User.class, userId);
        User u = userDAO.find(userId);

        if(!u.getEmail().equals(userEmail)){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
      
            /*Record rec = (Record) getEntityManager().createQuery("SELECT r FROM Record r WHERE r.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();*/
            
        Record rec = recordDAO.findByUserId(userId);
        if(rec==null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }else{
            return Response.ok().entity(rec).build();
        }
       
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
