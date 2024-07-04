/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAvailable;
import aes.persistence.AgendaAvailableDAO;
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
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("agendaavailable")
public class AgendaAvailableFacadeREST extends AbstractFacade<AgendaAvailable> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AgendaAvailableDAO availableDao;

    public AgendaAvailableFacadeREST() {
        super(AgendaAvailable.class);
        try {
            availableDao = new AgendaAvailableDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void insert(AgendaAvailable entity) {
        try {
            availableDao.insert(entity, em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @PUT
    @Path("update/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@PathParam("id") Long id, AgendaAvailable entity) {
        availableDao.update(id, entity, em);
    }

    //Testado e deu erro: javax.servlet.ServletException: javax.ejb.EJBException: Stateless SessionBean method returned without completing transaction
    @DELETE
    @Path("remove/{id}")
    public void remove(@PathParam("id") Long id) {
        availableDao.remove(id, em);
    }

    //Testado com sucesso. Obs: Tem que voltar apenas o id do usuario e id do consultor.
    @GET
    @Path("find/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AgendaAvailable find(@PathParam("id") Long id) {
        return availableDao.search(id, em);
    }

    //Testado com sucesso. Obs: Tem que voltar apenas o id do usuario e id do consultor.
    @Path("findAll")
    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public List<AgendaAvailable> findAll() {
        try {
            return availableDao.findAll(em);
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
