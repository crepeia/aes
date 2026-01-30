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
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
@Path("secured/mobileoptions")
@TransactionManagement(TransactionManagementType.BEAN)
@Secured
public class MobileOptionsFacadeREST extends AbstractFacade<MobileOptions> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private MobileOptionsDAO mobileOptionsDAO;
    private UserDAO userDAO;

    @Inject
    private SecurityContextHelper securityHelper;

    public MobileOptionsFacadeREST() {
        super(MobileOptions.class);
        try {
            mobileOptionsDAO = new MobileOptionsDAO();
            userDAO = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    /*
    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MobileOptions create(MobileOptions entity) {
        try {
            mobileOptionsDAO.insertOrUpdate(entity, em);
            return entity;
        } catch (SQLException e) {
            return null;
        }
    }
    */
    
    @Path("update")
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response update(MobileOptions entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            mobileOptionsDAO.edit(loggedUser, entity, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("edit/allowQuestionNotifications")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateAllowQuestionNotifications(Boolean allowQuestionNotifications) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            MobileOptions options = mobileOptionsDAO.find(loggedUser.getId(), em);
            
            if (options == null) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_OPTIONS_UPDATE reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                    
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            options.setAllowQuestionNotifications(allowQuestionNotifications);
            mobileOptionsDAO.edit(loggedUser, options, em);
            return Response.status(Response.Status.OK).build();
        } catch(SQLException | RuntimeException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("edit/changeNotificationToken")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response changeNotificationToken(String notificationToken) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            MobileOptions options = mobileOptionsDAO.find(loggedUser.getId(), em);
            
            if (options == null) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_OPTIONS_UPDATE reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                    
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            options.setNotificationToken(notificationToken);
            mobileOptionsDAO.edit(loggedUser, options, em);
            return Response.status(Response.Status.OK).build();
        } catch(SQLException | RuntimeException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("edit/changeAnonymousNotificationToken")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response changeAnonymousNotificationToken(String notificationToken) {
        try {
            Response r = securityHelper.requireAuthenticatedAnonymous();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            MobileOptions options = mobileOptionsDAO.find(loggedUser.getId(), em);
            
            if (options == null) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_OPTIONS_UPDATE reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                    
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            options.setNotificationToken(notificationToken);
            mobileOptionsDAO.edit(loggedUser, options, em);
            return Response.status(Response.Status.OK).build();
        } catch(SQLException | RuntimeException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            MobileOptions entity = mobileOptionsDAO.find(loggedUser.getId(), em);
            
            if (entity == null) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DID_NOT_FIND reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                    
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            return Response.ok().entity(entity).build();
        } catch (SQLException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findNotificationToken/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getNotificationToken(@PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            User targetUser = userDAO.find(userId, em);
            
            if (targetUser == null) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_GET_NOTIFICATION_TOKEN reason=TARGET_USER_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());

                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }
            
            boolean allowed = false;
            
            // 1) Mesmo usuário
            if (Objects.equals(loggedUser.getId(), targetUser.getId())) {
                allowed = true;
            }
            // 2) Consultor pode acessar qualquer usuário
            else if (loggedUser.isConsultant()) {
                allowed = true;
            }
            // 3) Usuário acessando consultor relacionado
            else if (securityHelper.isValidUserConsultantRelation(loggedUser, targetUser)) {
                allowed = true;
            }
            
            if (!allowed) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_GET_NOTIFICATION_TOKEN reason=ACCESS_DENIED "
                       + "actorUserId={0} targetUserId={1}",
                         new Object[]{loggedUser.getId(), targetUser.getId()});

                return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
            }
            
            MobileOptions options = mobileOptionsDAO.find(targetUser.getId(), em);
            if (options == null || options.getNotificationToken() == null) {
                Logger.getLogger(MobileOptionsFacadeREST.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] DID_NOT_FIND reason=TARGET_OBJECT_NOT_FOUND "
                   + "actorUserId={0}",
                     loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            return Response.ok(options.getNotificationToken()).build();
        } catch(SQLException | RuntimeException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findConsultantNotificationTokens")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getConsultantNotificationTokens() {
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Logger.getLogger(MobileOptionsFacadeREST.class.getName())
            .log(Level.INFO,
                 "[SECURITY] ACCESS_CONSULTANT_NOTIFICATION_TOKENS actorUserId={0}",
                 loggedUser.getId());
            
            List<User> consultants = userDAO.list("consultant", true, em);
            List<String> tokens = new ArrayList<>();
            
            for (User consultant : consultants) {
                MobileOptions options = mobileOptionsDAO.find(consultant.getId(), em);
                if (options != null && options.getNotificationToken() != null) {
                    tokens.add(options.getNotificationToken());
                }
            }
            
            return Response.ok().entity(tokens).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(MobileOptionsFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
