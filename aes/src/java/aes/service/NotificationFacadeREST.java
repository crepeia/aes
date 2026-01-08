/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Notification;
import aes.model.User;
import aes.persistence.NotificationDAO;
import aes.persistence.UserDAO;
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
 * @author Leonorico
 */
@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("notification")
public class NotificationFacadeREST extends AbstractFacade<Notification> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private NotificationDAO notificationDao;
    private UserDAO userDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public NotificationFacadeREST() {
        super(Notification.class);
        try {
            notificationDao = new NotificationDAO();
            userDao = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
        }
    }

    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(Notification notification) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            boolean allowed = false;
            
            User targetUser;
            
            if (loggedUser.isConsultant()) {
                targetUser = userDao.find(notification.getUser().getId(), em);
                
                if (securityHelper.isValidUserConsultantRelation(targetUser, loggedUser)) {
                    notification.setConsultant(loggedUser);
                    notification.setUser(targetUser);
                    allowed = true;
                }
            } else {
                targetUser = userDao.find(notification.getUser().getId(), em);
                
                if (securityHelper.isValidUserConsultantRelation(loggedUser, targetUser)) {
                    notification.setUser(targetUser);
                    allowed = true;
                }
            }
            
            if (allowed) {
                notificationDao.insert(notification, em);
                return Response.status(Response.Status.CREATED).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_CONSULTANT_RELATION").build();
            }
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    /*
    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response update(Notification notification) {
        try {
            notificationDao.update(notification, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    

    @DELETE
    @Path("delete/{id}")
    public Response delete(@PathParam("id") Long id) {
        Notification notification;
        try {
            if(notificationDao.find(id,em) != null) {
                notification = new Notification(id);
                notificationDao.delete(notification, em);
                return Response.status(Response.Status.OK).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        try {
            return Response.ok().entity(notificationDao.listOnce("id", id, em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("findAll")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllNotifications() {
        try {
            return Response.ok().entity(notificationDao.list(em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    */
    
    @GET
    @Path("findAllUnreadByUser")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllUnreadByUser() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<Notification> unreadNotifications = notificationDao.listUnreadByUser(loggedUser.getId(), em);
            
            for(Notification notification : unreadNotifications) {
                notification.setNotificated(true);
                notificationDao.update(notification, em);
            }
            return Response.ok().entity(unreadNotifications).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
