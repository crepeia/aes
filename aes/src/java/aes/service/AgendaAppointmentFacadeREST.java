/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAppointment;
import aes.persistence.AgendaAppointmentDAO;
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
 * @author Malder
 */


//TODO: Pesquisar sobre Secured e segurança do serviço REST para evitar dos serviços REST serem usados
//por fora do aplicativo/site. Evitar de fazer as requisiçoes por meio de um software, tipo POSTMAN.
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

    //Aqui os dados seriam inseridos via ativação do serviço REST, de modo que seria enviado um appointment
    //com id de usuario, id de consultor e data de appointment, sem id do appointment.
    
    //Dúvida: seria inserido um appointment inteiro já com o usuario e consultor setados, ou aconteceria
    //algum tipo de serialização/desserialização?
    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void insert(AgendaAppointment entity) {
        try {
            appointmentDao.insert(entity, em);
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

    //Testado e deu erro: javax.servlet.ServletException: javax.ejb.EJBException: Stateless SessionBean method returned without completing transaction
    @DELETE
    @Path("remove")
    public void remove(AgendaAppointment appointment) {
        try {
            appointmentDao.remove(appointment, em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Testado com sucesso. Obs: Tem que voltar apenas o id do usuario e id do consultor.
    @GET
    @Path("find/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AgendaAppointment find(@PathParam("id") Long id) {
        return appointmentDao.search(id, em);
    }

    //Testado com sucesso. Obs: Tem que voltar apenas o id do usuario e id do consultor.
    @Path("findAll")
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AgendaAppointment> findAll() {
        try {
            return appointmentDao.findAll(em);
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
