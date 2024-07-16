//Serviço REST que possibilita a manipulação de objetos do tipo AgendaAvailable e sua persistência no banco de dados

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AgendaAvailable;
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

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("agendaavailable")
public class AgendaAvailableFacadeREST extends AbstractFacade<AgendaAvailable> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private GenericDAO<AgendaAvailable> availableDao;

    public AgendaAvailableFacadeREST() {
        super(AgendaAvailable.class);
        try {
            availableDao = new GenericDAO<>(AgendaAvailable.class);
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void insert(AgendaAvailable available) {
        try {
            availableDao.insert(available, em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(AgendaAvailable available) {
        try {
            availableDao.update(available, em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @DELETE
    @Path("delete/{id}")
    public void delete(@PathParam("id") Long id) {
        try {
            availableDao.delete(availableDao.find(id, em), em);
        } catch (SQLException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public AgendaAvailable find(@PathParam("id") Long id) {
        return availableDao.find(id, em);
    }

    @Path("findAll")
    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AgendaAvailable> findAll() {
        try {
            return availableDao.list(em);
        } catch (SQLException ex) {
            return null;
        }
    }
        
    @Path("findAllByUser/{userId}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AgendaAvailable> findAllByUser(@PathParam("userId") Long userId) {
        try {
            return availableDao.list("user.id", userId, em);
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
