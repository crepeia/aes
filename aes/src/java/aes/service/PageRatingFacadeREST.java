/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Item;
import aes.model.Rating;
import aes.persistence.GenericDAO;
import aes.persistence.RatingDAO;
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
@Path("pagerating")
public class PageRatingFacadeREST extends AbstractFacade<Rating> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private RatingDAO pageRatingDao;

    public PageRatingFacadeREST() {
        super(Rating.class);
        try {
            pageRatingDao = new RatingDAO();
        } catch (NamingException ex) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @Path("ratePage")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response ratePage(Rating rating) {
        try {
            pageRatingDao.insertOrUpdate(rating, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
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
    
    @Path("findLastUserRatingByItem/{userId}/{itemId}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findLastUserRatingByItem(@PathParam("userId") Long userId, @PathParam("itemId") Long itemId) {
        try {
            List<Rating> result = pageRatingDao.listRatingByUserIdAndItemId(userId, itemId, em);
            if(!result.isEmpty())
                return Response.ok().entity(result.get(0)).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
