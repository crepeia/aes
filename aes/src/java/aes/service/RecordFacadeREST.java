/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Record;
import aes.model.User;
import aes.utility.Secured;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
public class RecordFacadeREST extends AbstractFacade<Record> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    @Context
    SecurityContext securityContext;

    public RecordFacadeREST() {
        super(Record.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRecord(Record entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        User u = em.find(User.class, entity.getUser().getId());
        if (!u.getEmail().equals(userEmail)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Record created = super.create(entity);
            return Response.ok().entity(created).build();
        } catch (Exception e) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("create/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(@PathParam("userId") Long userId) {
        //String userEmail = securityContext.getUserPrincipal().getName();

        try {
            Record entity = new Record();
            entity.setUser(em.find(User.class, userId));
            entity.setDailyGoal(0);
            entity.setWeeklyGoal(0);
            super.create(entity);
            return Response.ok().entity(entity).build();
        } catch (Exception e) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @PUT
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Record edit(Record entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        User u = em.find(User.class, entity.getUser().getId());
        if (u.getEmail().equals(userEmail)) {
            super.edit(entity);
        }

        return entity;
    }

    @GET
    @Path("find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userId") Long userId) {
        String userEmail = securityContext.getUserPrincipal().getName();
        User u = em.find(User.class, userId);
        if(!u.getEmail().equals(userEmail)){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Record rec = (Record) getEntityManager().createQuery("SELECT r FROM Record r WHERE r.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Response.ok().entity(rec).build();
        } catch (NoResultException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            Logger.getLogger(RecordFacadeREST.class.getName()).log(Level.ALL.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
