/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;


import aes.model.DailyLog;
import aes.model.MedalUser;
import aes.model.User;
import aes.persistence.MedalUserDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
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
 * @author Matheus Carvalho
 */
@Stateless
@Path("medaluser")
public class MedalUserFacadeREST extends AbstractFacade<MedalUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public MedalUserFacadeREST() {
        super(MedalUser.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("find/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByUser(@PathParam("userId") String uId) {
        try {
            List<MedalUser> list = (List<MedalUser>) em.createQuery("SELECT mu FROM MedalUser mu WHERE mu.user.id=:userId")
                .setParameter("userId", Long.parseLong(uId))
                .getResultList();
            
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createMedal(MedalUser entity) {
        try {
            List<MedalUser> meList
                    = em.createQuery("SELECT me FROM MedalUser me WHERE me.user.id=:userId AND me.medal.id=:medalId AND me.description=:medalDescription")
                            .setParameter("userId", entity.getUser().getId())
                            .setParameter("medalId", entity.getMedal().getId())
                            .setParameter("medalDescription", entity.getDescription())
                            .getResultList();
            if (meList.isEmpty()) {
                MedalUser newEntity = super.create(entity);
            }
            return true;
            
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    @POST
    @Path("createInitialMedalAA")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createInitialMedalAA(MedalUser entity) {
        try {
            List<MedalUser> meList
                    = em.createQuery("SELECT me FROM MedalUser me WHERE me.user.id=:userId AND me.medal.id=:medalId")
                            .setParameter("userId", entity.getUser().getId())
                            .setParameter("medalId", entity.getMedal().getId())
                            .getResultList();
            if (meList.isEmpty()) {
                MedalUser newEntity = super.create(entity);
            }
            return true;
            
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    
    @POST
    @Path("updateMedal")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean updateMedal(MedalUser entity) {
        try {
            MedalUser mu = (MedalUser) em.createQuery("SELECT mu from MedalUser mu WHERE mu.user.id=:userId and mu.medal.id=:medalId")
                .setParameter("userId", entity.getUser().getId())
                .setParameter("medalId", entity.getMedal().getId())
                .getSingleResult();
            
        mu.setDescription(entity.getDescription());
        MedalUser newEntity = super.edit(mu);
        return true;   
                
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    
    @GET
    @Path("getMonthlyDrinkMedal/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMonthlyDrinkMedal(@PathParam("userId") Long userId) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        
        try {
            return getEntityManager()
                    .createQuery("SELECT COUNT(*) FROM ChallengeUser c WHERE month(c.dateCreated)=:monthNumber and c.user.id=:userId and c.challenge.id=:challengeId")
                    .setParameter("monthNumber", month)
                    .setParameter("userId", userId)   
                    .setParameter("challengeId", 5L)
                    .getSingleResult().toString();  
        } catch (Exception e) {
            return "0";
        }
    }
    
    
    @GET
    @Path("getMonthlyNotDrinkMedal/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMonthlyNotDrinkMedal(@PathParam("userId") Long userId) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        
        try {
            return getEntityManager()
                    .createQuery("SELECT COUNT(*) FROM ChallengeUser c WHERE month(c.dateCreated)=:monthNumber and c.user.id=:userId and c.challenge.id=:challengeId")
                    .setParameter("monthNumber", month)
                    .setParameter("userId", userId)   
                    .setParameter("challengeId", 6L)
                    .getSingleResult().toString();  
        } catch (Exception e) {
            return "0";
        }
    }


    @GET
    @Path("getDrinkLog/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrinkLog(@PathParam("userId") Long userId) {        
        try {
            List<DailyLog> list = (List<DailyLog>) em.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:userId ORDER BY dl.logDate DESC")
            .setParameter("userId", userId)   
            .getResultList(); 

            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    protected long getPointsFromDate(User u, LocalDate date) {
        Long score = (Long) this.getEntityManager()
                .createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.dateCompleted > :date AND c.user.id=:userId")
                .setParameter("date", date)
                .setParameter("userId", u.getId()).getSingleResult();
        if (score == null) {
            score = Long.valueOf(0);
        }
        return score;
    }

}
