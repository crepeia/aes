/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Item;
import aes.model.Rating;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.persistence.RatingDAO;
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
@Path("pagerating")
public class PageRatingFacadeREST extends AbstractFacade<Rating> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private RatingDAO pageRatingDao;
    private GenericDAO<Item> itemDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public PageRatingFacadeREST() {
        super(Rating.class);
        try {
            pageRatingDao = new RatingDAO();
            itemDao = new GenericDAO(Item.class);
        } catch (NamingException ex) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @Path("ratePage")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response ratePage(Rating rating) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (rating == null || rating.getItem() == null || rating.getRelevant() == null) {
                Logger.getLogger(PageRatingFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_RATING_INSERT reason=INVALID_DATA ");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            Item item = itemDao.find(rating.getItem().getId(), em);
            
            if (item == null || item.getName() == null) {
                Logger.getLogger(PageRatingFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_RATING_INSERT reason=INVALID_DATA ");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            rating.setItem(item);
            rating.setUser(loggedUser);
            
            pageRatingDao.insertOrUpdate(rating, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Path("updateRate/{id}")
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateRate(@PathParam("id") Long id, Rating rating) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (rating == null || rating.getItem() == null || rating.getRelevant() == null) {
                Logger.getLogger(PageRatingFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_RATING_INSERT reason=INVALID_DATA ");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            Item item = itemDao.find(rating.getItem().getId(), em);
            
            if (item == null || item.getName() == null) {
                Logger.getLogger(PageRatingFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_RATING_INSERT reason=INVALID_DATA ");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            Rating oldRating = pageRatingDao.find(id, em);
            
            r = securityHelper.requireSameUser(oldRating.getUser(), loggedUser.getId());
            if (r != null) return r;
            
            oldRating.setRelevant(rating.getRelevant());
            oldRating.setDateRated(rating.getDateRated());

            pageRatingDao.update(oldRating, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    /*
    @Path("find/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response find(@PathParam("id") Long id) {
        try {
            List<Rating> result = pageRatingDao.listOnce("id", id, em);
            if(!result.isEmpty())
                return Response.ok().entity(result).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Path("findAll")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findAllPageRatings() {
        try {
            return Response.ok().entity(pageRatingDao.list(em)).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("findAllUserRatings/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findAllUserRatings(@PathParam("id") Long id) {
        try {
            List<Rating> result = pageRatingDao.list("user.id", id, em);
            if(!result.isEmpty())
                return Response.ok().entity(result).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    */
    
    @Path("findLastUserRatingByItem/{itemId}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findLastUserRatingByItem(@PathParam("itemId") Long itemId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Item item = itemDao.find(itemId, em);
            
            if (item == null) {
                Logger.getLogger(PageRatingFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_RATING_INSERT reason=INVALID_DATA ");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            List<Rating> result = pageRatingDao.listRatingByUserIdAndItemId(loggedUser.getId(), item.getId(), em);
            if(result.isEmpty()) {
                Logger.getLogger(PageRatingFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_APPOINTMENT_FIND reason=TARGET_USER_NOT_FOUND");
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
                
            return Response.ok().entity(result.get(0)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
