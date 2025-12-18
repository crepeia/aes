/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAvailable;
import aes.model.User;
import aes.persistence.AgendaAvailableDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
@Path("agendaavailable")
public class AgendaAvailableFacadeREST extends AbstractFacade<AgendaAvailable> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AgendaAvailableDAO availableDao;
    
    @Inject
    private SecurityContextHelper securityHelper;
    
    private static final Set<LocalTime> ALLOWED_HOURS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0),
            LocalTime.of(18, 0), LocalTime.of(19, 0), LocalTime.of(20, 0)
        ))
    );

    public AgendaAvailableFacadeREST() {
        super(AgendaAvailable.class);
        try {
            availableDao = new AgendaAvailableDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    private Response validateAvailableDayAndTime(Byte weekDay, LocalTime time) {
        if (weekDay == null || weekDay < 0 || weekDay > 4) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Dia da semana inválido")
                .build();
        }
        
        if (time == null || !ALLOWED_HOURS.contains(time)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Horário não permitido")
                .build();
        }
        
        return null;
    }
    
    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(AgendaAvailable available) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireRegularUser(loggedUser);
            if (r != null) return r;
            
            available.setUser(loggedUser);
            
            Response validation = validateAvailableDayAndTime(
                available.getAvailableDate(),
                available.getAvailableTime()
            );
            if (validation != null) return validation;
            
            availableDao.insert(available, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("delete/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            AgendaAvailable available = availableDao.find(id, em);
            if (available == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            r = securityHelper.requireSameUser(loggedUser, available.getUser().getId());
            if (r != null) return r;
            
            availableDao.delete(available, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
        
    @Path("findAllByUser/{userId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllByUser(@PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireRegularSameUser(userId);
            if (r != null) return r;
            
            return Response.ok().entity(availableDao.list("user.id", userId, em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("findAllByConsultant/{consultantId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllByConsultant(@PathParam("consultantId") Long consultantId) {       
        try {
            Response r = securityHelper.requireConsultantSameUser(consultantId);
            if (r != null) return r;
            
            return Response.ok().entity(availableDao.listByConsultant(consultantId, em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
