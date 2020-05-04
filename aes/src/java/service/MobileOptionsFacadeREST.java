/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import aes.model.MobileOptions;
import aes.model.User;
import aes.utility.Secured;
import java.util.Calendar;
import java.util.Date;
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
@Path("secured/mobileoptions")
public class MobileOptionsFacadeREST extends AbstractFacade<MobileOptions> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public MobileOptionsFacadeREST() {
        super(MobileOptions.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(MobileOptions entity) {
        super.create(entity);
    }

    @PUT
    @Path("edit/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("userId") Long userId, MobileOptions entity) {
        try {
            entity.setUser(em.find(User.class, userId));
            super.edit(entity);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public MobileOptions find(@PathParam("userId") Long userId) {
        try {
           return (MobileOptions) getEntityManager().createQuery("SELECT mo FROM MobileOptions mo WHERE mo.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch(NoResultException e) {
            MobileOptions entity = new MobileOptions();
            entity.setUser(em.find(User.class, userId));
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY,12);
            cal.set(Calendar.MINUTE,00);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);


            entity.setAllowTipNotifications(false);
            entity.setTipNotificationTime(cal.getTime());
            
            cal.set(Calendar.HOUR_OF_DAY,19);
            
            entity.setAllowDrinkNotifications(false);
            entity.setDrinkNotificationTime(cal.getTime());

            entity.setNotificationToken("");

            super.create(entity);
            return entity;
            
        } catch(Exception e) {
            return null;
        }
    }



    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
