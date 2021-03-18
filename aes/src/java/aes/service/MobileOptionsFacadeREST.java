/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.MobileOptions;
import aes.model.User;
import aes.utility.Secured;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Calendar;
import java.util.Date;
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
@Path("secured/mobileoptions")
public class MobileOptionsFacadeREST extends AbstractFacade<MobileOptions> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    @Context
    SecurityContext securityContext;

    public MobileOptionsFacadeREST() {
        super(MobileOptions.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MobileOptions create(MobileOptions entity) {
        try {
            super.create(entity);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @PUT
    @Path("edit/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("userId") Long userId, MobileOptions entity) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();
            User u = em.find(User.class, userId);
            if (u.getEmail().equals(userEmail)) {
                entity.setUser(u);
                entity.setDrinkNotificationTime(entity.getDrinkNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));
                entity.setTipNotificationTime(entity.getTipNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));

                super.edit(entity);
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userId") Long userId) {
        try {
            MobileOptions op = (MobileOptions) getEntityManager().createQuery("SELECT mo FROM MobileOptions mo WHERE mo.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Response.ok().entity(op).build();
        } catch (NoResultException e) {
            MobileOptions entity = new MobileOptions();
            entity.setUser(em.find(User.class, userId));

            entity.setAllowTipNotifications(false);
            entity.setTipNotificationTime(OffsetTime.of(12, 0, 0, 0, OffsetDateTime.now().getOffset()));

            entity.setAllowDrinkNotifications(false);
            entity.setDrinkNotificationTime(OffsetTime.of(19, 0, 0, 0, OffsetDateTime.now().getOffset()));

            entity.setNotificationToken("");

            super.create(entity);
            return Response.ok().entity(entity).build();

        } catch (Exception e) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
