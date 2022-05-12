/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Challenge;
import aes.persistence.ChallengeDAO;
import aes.persistence.ContactDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
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
 * @author bruno
 */
@Stateless
@Path("secured/challenge")
@Secured
public class ChallengeFacadeREST extends AbstractFacade<Challenge> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ChallengeDAO challengeDAO;

    public ChallengeFacadeREST() {
        super(Challenge.class);
         try {
            challengeDAO = new ChallengeDAO();
        } catch (NamingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Challenge find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Path("all")
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Challenge> findAll() {
        return super.findAll();
    }
    
    @GET
    @Path("type/{type}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Challenge> findAllByType(@PathParam("type") Challenge.ChallengeType ct) {
        return getEntityManager().createQuery("SELECT c FROM Challenge c WHERE c.type=:type")
                .setParameter("type", ct)
                .getResultList();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
