/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.MedalUser;
import aes.model.TitleUser;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
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
 * @author LEVTY
 */
@Stateless
@Path("titleuser")
public class TitleUserFacadeREST extends AbstractFacade<TitleUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public TitleUserFacadeREST() {
        super(TitleUser.class);
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createTitle(TitleUser entity) {
        try {
            List<TitleUser> teList
                    = em.createQuery("SELECT te FROM TitleUser te WHERE te.user.id =: userId AND te.title.id =: titleId AND te.description =: titleDescription")
                            .setParameter("userId", entity.getUser().getId())
                            .setParameter("titleId", entity.getTitle().getId() )
                            .setParameter("titleDescription", entity.getDescription())
                            .getResultList();
            if (teList.isEmpty()) {
                TitleUser newEntity = super.create(entity);
            }
            return true;
            
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
     
    @GET
    @Path("find/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByUser(@PathParam("userId") String uId) {
        try {
            List<TitleUser> list = (List<TitleUser>) em.createQuery("SELECT te FROM TitleUser te WHERE te.user.id =: userId")
                .setParameter("userId", Long.parseLong(uId))
                .getResultList();
            
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("findAll")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllTitle() {
        try {
            List<TitleUser> list = (List<TitleUser>) em.createQuery("SELECT te FROM TitleUser te").getResultList();
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
