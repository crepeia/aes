/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAppointment;
import aes.model.User;
import aes.persistence.AgendaAppointmentDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author Leonorico
 */

@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("agendaappointment")
public class AgendaAppointmentFacadeREST extends AbstractFacade<AgendaAppointment> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AgendaAppointmentDAO appointmentDao;
    private UserDAO userDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public AgendaAppointmentFacadeREST() {
        super(AgendaAppointment.class);
        try {
            appointmentDao = new AgendaAppointmentDAO();
            userDao = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    private boolean canAccessAppointment(User loggedUser, AgendaAppointment appointment) {
        if (loggedUser == null || appointment == null) {
            return false;
        }
        
        if (loggedUser.isConsultant()) {
            return appointment.getConsultant() != null &&
                   Objects.equals(
                       appointment.getConsultant().getId(),
                       loggedUser.getId()
                   );
        }

        return appointment.getUser() != null &&
               Objects.equals(
                   appointment.getUser().getId(),
                   loggedUser.getId()
               );
    }
    
    private Response validateAppointmentAccess(User loggedUser, AgendaAppointment appointment) {
        if (!canAccessAppointment(loggedUser, appointment)) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] DENIED_APPOINTMENT_ACCESS reason=INVALID_USER_OBJECT_RELATION "
                   + "actorUserId={0} appointmentId={1} role={2}",
                     new Object[]{
                         loggedUser.getId(),
                         appointment.getId(),
                         loggedUser.isConsultant() ? "CONSULTANT" : "REGULAR"
                     });
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_OBJECT_RELATION").build();
        }
        return null;
    }

    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(AgendaAppointment appointment) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (loggedUser.isConsultant()) {
                User user = userDao.find(appointment.getUser().getId(), em);
                
                if (user == null) {
                    Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                        .log(Level.WARNING,
                             "[SECURITY] DENIED_APPOINTMENT_INSERT reason=TARGET_USER_NOT_FOUND "
                           + "actorUserId={0} role=CONSULTANT",
                             loggedUser.getId());
                    return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
                }
                
                if (!securityHelper.isValidUserConsultantRelation(user, loggedUser)) {
                    return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_CONSULTANT_RELATION").build();
                }
                
                appointment.setConsultant(loggedUser);
                appointment.setUser(user);
            } else {
                User consultant = userDao.find(appointment.getConsultant().getId(), em);
                
                if (consultant == null) {
                    Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                        .log(Level.WARNING,
                             "[SECURITY] DENIED_APPOINTMENT_INSERT reason=TARGET_USER_NOT_FOUND "
                           + "actorUserId={0} role=REGULAR",
                             loggedUser.getId());
                    
                    return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
                }
                
                if (!securityHelper.isValidUserConsultantRelation(loggedUser, consultant)) {
                    return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_CONSULTANT_RELATION").build();
                }
                
                appointment.setUser(loggedUser);
                appointment.setConsultant(consultant);
            }
            
            appointmentDao.insert(appointment, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @DELETE
    @Path("delete/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            AgendaAppointment appointment = appointmentDao.find(id, em);
            if (appointment == null) {
                Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                        .log(Level.WARNING,
                             "[SECURITY] DENIED_APPOINTMENT_DELETE reason=TARGET_OBJECT_NOT_FOUND "
                           + "actorUserId={0}",
                             loggedUser.getId());
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            Response access = validateAppointmentAccess(loggedUser, appointment);
            if (access != null) return access;
            
            appointmentDao.delete(appointment, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            AgendaAppointment appointment = appointmentDao.find(id, em);
            if (appointment == null) {
                Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                        .log(Level.WARNING,
                             "[SECURITY] DENIED_APPOINTMENT_FIND reason=TARGET_OBJECT_NOT_FOUND "
                           + "actorUserId={0}",
                             loggedUser.getId());
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            Response access = validateAppointmentAccess(loggedUser, appointment);
            if (access != null) return access;
            
            return Response.ok(appointment).build();
        } catch (RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Path("findAllCurrentByUser/{userId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllCurrentByUser(@PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireRegularSameUser(userId);
            if (r != null) return r;
            
            return Response.ok(
                appointmentDao.listCurrentByUser(userId, em)
            ).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Path("findAllByConsultant/{consultantId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllByConsultant(@PathParam("consultantId") Long consultantId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            User consultant = userDao.find(consultantId, em);
            
            if (consultant == null) {
                Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] DENIED_APPOINTMENT_FIND reason=TARGET_USER_NOT_FOUND "
                   + "actorUserId={0} consultantId={1}",
                     new Object[]{loggedUser.getId(), consultantId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }
            
            if (loggedUser.isConsultant()) {
                r = securityHelper.requireSameConsultant(loggedUser, consultantId);
                if (r != null) return r;
            } else {
                if (!securityHelper.isValidUserConsultantRelation(loggedUser, consultant)) {
                    return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_CONSULTANT_RELATION").build();
                }
            }
            
            return Response.ok(
                appointmentDao.list("consultant.id", consultantId, em)
            ).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
   
    @GET
    @Path("findAppointmentsByUserAndDate")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAppointmentsByUserAndDate(@QueryParam("userId") Long userId, @QueryParam("date") String date) throws SQLException {
        try {
            Response r = securityHelper.requireRegularSameUser(userId);
            if (r != null) return r;
            
            return Response.ok(
                appointmentDao.findAppointmentsByUserAndDate(userId, date, em)
            ).build();
        } catch (RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }    
}
