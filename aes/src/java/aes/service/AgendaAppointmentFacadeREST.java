//Serviço REST que possibilita a manipulação de objetos do tipo AgendaAppointment e sua persistência no banco de dados

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAppointment;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.List;
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

/**
 *
 * @author Leonorico
 */

//TODO: Pesquisar sobre Secured e segurança do serviço REST para evitar dos serviços REST serem usados
//por fora do aplicativo/site. Evitar de fazer as requisiçoes por meio de um software, tipo POSTMAN.
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("agendaappointment")
public class AgendaAppointmentFacadeREST extends AbstractFacade<AgendaAppointment> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private GenericDAO<AgendaAppointment> appointmentDao;

    public AgendaAppointmentFacadeREST() {
        super(AgendaAppointment.class);
        try {
            appointmentDao = new GenericDAO<>(AgendaAppointment.class);
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void insert(AgendaAppointment appointment) {
        try {
            appointmentDao.insert(appointment, em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(AgendaAppointment appointment) {
        try {
            appointmentDao.update(appointment, em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @DELETE
    @Path("delete/{id}")
    public void delete(@PathParam("id") Long id) {
        try {
            appointmentDao.delete(appointmentDao.find(id, em), em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public AgendaAppointment find(@PathParam("id") Long id) {
        return appointmentDao.find(id, em);
    }

    @Path("findAll")
    @Override
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AgendaAppointment> findAll() {
        try {
            return appointmentDao.list(em);
        } catch (SQLException ex) {
            return null;
        }
    }
    
    @Path("findAllByUser/{userId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AgendaAppointment> findAllByUser(@PathParam("userId") Long userId) {
        try {
            return appointmentDao.list("user.id", userId, em);
        } catch (SQLException ex) {
            return null;
        }
    }
    
    @Path("findAllByConsultant/{consultantId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AgendaAppointment> findAllByConsultant(@PathParam("consultantId") Long consultantId) {
        try {
            return appointmentDao.list("consultant.id", consultantId, em);
        } catch (SQLException ex) {
            return null;
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
