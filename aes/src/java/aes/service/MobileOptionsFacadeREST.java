/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.MobileOptions;
import aes.model.User;
import aes.persistence.MobileOptionsDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
@Path("secured/mobileoptions")
@TransactionManagement(TransactionManagementType.BEAN)
public class MobileOptionsFacadeREST extends AbstractFacade<MobileOptions> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private MobileOptionsDAO mobileOptionsDAO;
    private UserDAO userDAO;

    @Context
    SecurityContext securityContext;

    public MobileOptionsFacadeREST() {
        super(MobileOptions.class);
        try {
            mobileOptionsDAO = new MobileOptionsDAO();
            userDAO = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MobileOptions create(MobileOptions entity) {
        try {
            //super.create(entity);
            mobileOptionsDAO.insertOrUpdate(entity, em);
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
            User u = userDAO.find(userId, em);
            if (u.getEmail().equals(userEmail)) {
//                entity.setUser(u);
//                entity.setDrinkNotificationTime(entity.getDrinkNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));
//                entity.setTipNotificationTime(entity.getTipNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));
//
//                super.edit(entity);

                  mobileOptionsDAO.edit(userId, entity, em);
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @PUT
    @Path("edit/allowQuestionNotifications/{userId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateAllowQuestionNotifications(@PathParam("userId") Long userId, Boolean allowQuestionNotifications) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();
            User u = userDAO.find(userId, em);
            if(u.getEmail().equals(userEmail)) {
                MobileOptions options = mobileOptionsDAO.find(userId, em);
                options.setAllowQuestionNotifications(allowQuestionNotifications);
                mobileOptionsDAO.edit(userId, options, em);
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch(SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("edit/changeNotificationToken/{userId}/{token}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response changeNotificationToken(@PathParam("userId") Long userId, @PathParam("token") String notificationToken) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();
            User u = userDAO.find(userId, em);
            if(u.getEmail().equals(userEmail)) {
                MobileOptions options = mobileOptionsDAO.find(userId, em);
                options.setNotificationToken(notificationToken);
                mobileOptionsDAO.edit(userId, options, em);
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch(SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userId") Long userId) {
        try {
           /* MobileOptions op = (MobileOptions) getEntityManager().createQuery("SELECT mo FROM MobileOptions mo WHERE mo.user.id=:userId")
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

            super.create(entity);*/
            MobileOptions entity = mobileOptionsDAO.find(userId, em);
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
