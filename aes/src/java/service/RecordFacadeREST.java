/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import aes.model.Record;
import aes.model.User;
import aes.utility.Secured;
import java.util.List;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    public RecordFacadeREST() {
        super(Record.class);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Record create(Record entity) {
        try {
            return super.create(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @POST
    @Path("create/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Record create(@PathParam("userId") Long userId) {
        try {
            Record entity = new Record();
            entity.setUser(em.find(User.class, userId));
            entity.setDailyGoal(null);
            entity.setWeeklyGoal(null);
            super.create(entity);
            return entity;
        } catch (Exception e) {
            return null;
    }

    }
    
    @PUT
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public void edit(Record entity) {
        super.edit(entity);
    }
    

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Record find(@PathParam("userId") Long userId) {
        try {
            return (Record) getEntityManager().createQuery("SELECT r FROM Record r WHERE r.user.id=:userId")
                    .setParameter("userId",userId)
                    .getSingleResult();
            
        } catch(NoResultException e) {
            return null;
            /*
            Record entity = new Record();
            entity.setUser(em.find(User.class, userId));
            entity.setDailyGoal(null);
            entity.setWeeklyGoal(null);
            super.create(entity);
            return entity;
            */
        } catch(Exception e) {
            return null;
    }
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Record> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Record> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
