//Serviço REST que possibilita a manipulação de objetos do tipo AgendaAppointment e sua persistência no banco de dados

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAppointment;
import aes.persistence.AgendaAppointmentDAO;
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
@Path("agendaappointment")
public class AgendaAppointmentFacadeREST extends AbstractFacade<AgendaAppointment> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AgendaAppointmentDAO appointmentDao;

    public AgendaAppointmentFacadeREST() {
        super(AgendaAppointment.class);
        try {
            appointmentDao = new AgendaAppointmentDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(AgendaAppointment appointment) {
        try {
            appointmentDao.insert(appointment, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response update(AgendaAppointment appointment) {
        try {
            appointmentDao.update(appointment, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("delete/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            AgendaAppointment app = new AgendaAppointment(id);
            appointmentDao.delete(app, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        return Response.ok().entity(appointmentDao.find(id, em)).build();
    }

    @Path("findAll")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllAppointments() {
        try {
            return Response.ok().entity(appointmentDao.list(em)).build();
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("findAllCurrentByUser/{userId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllCurrentByUser(@PathParam("userId") Long userId) {
        try {
            return Response.ok().entity(appointmentDao.listCurrentByUser(userId, em)).build();
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("findAllByConsultant/{consultantId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllByConsultant(@PathParam("consultantId") Long consultantId) {
        try {
            return Response.ok().entity(appointmentDao.list("consultant.id", consultantId, em)).build();
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
