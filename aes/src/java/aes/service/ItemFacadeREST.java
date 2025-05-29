/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Item;
import aes.persistence.GenericDAO;
import aes.utility.Secured;
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
import javax.ws.rs.GET;
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
@Path("item")
public class ItemFacadeREST extends AbstractFacade<Item> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private GenericDAO<Item> itemDao;

    public ItemFacadeREST() {
        super(Item.class);
        try {
            itemDao = new GenericDAO(Item.class);
        } catch (NamingException ex) {
            Logger.getLogger(ItemFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @Path("find/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response find(@PathParam("id") Long id) {
        try {
            List<Item> result = itemDao.listOnce("id", id, em);
            if(!result.isEmpty())
                return Response.ok().entity(result).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(ItemFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Path("findAll")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findAllItems() {
        try {
            return Response.ok().entity(itemDao.list(em)).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(ItemFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
