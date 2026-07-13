/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Item;
import aes.model.User;
import aes.persistence.GenericDAO;
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
@Path("item")
public class ItemFacadeREST extends AbstractFacade<Item> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private GenericDAO<Item> itemDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public ItemFacadeREST() {
        super(Item.class);
        itemDao = new GenericDAO(Item.class);
    }
    
    @Path("addItem")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addItem(Item item) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (item == null || item.getName() == null || item.getType() == null) {
                Logger.getLogger(ItemFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_ITEM reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            itemDao.insertOrUpdate(item, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ItemFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    /*
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
    */
    
    @Path("findByName/{name}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findByName(@PathParam("name") String name) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (name.isEmpty()) {
                Logger.getLogger(ItemFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_ITEM_NAME reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            List<Item> result = itemDao.listOnce("name", name, em);
            
            if (result.isEmpty()) {
                Logger.getLogger(AgendaAppointmentFacadeREST.class.getName())
                    .log(Level.INFO,
                         "[INFO] DID_NOT_FIND_ITEM reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                    
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            return Response.ok().entity(result).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ItemFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
