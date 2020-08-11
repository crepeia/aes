/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.controller.ChallengeUserController;
import aes.model.ChallengeUser;
import aes.model.Challenge;
import aes.model.User;
import aes.utility.Secured;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javafx.util.Pair;
import javax.ejb.Stateless;
import javax.inject.Inject;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@Path("secured/challengeuser")
public class ChallengeUserFacadeREST extends AbstractFacade<ChallengeUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    @Context
    SecurityContext securityContext;
    
    @Inject
    private ChallengeUserController challengeUserController;
    
    
    
    public ChallengeUserFacadeREST() {
        super(ChallengeUser.class);
    }

    @POST
    @Path("createChallenge/{challengeId}/{userId}/{date}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void createChallenge(@PathParam("challengeId") Long challengeId, @PathParam("userId") Long userId, @PathParam("date") String dateCreated) {
        try{
            Challenge c = em.find(Challenge.class, challengeId);
            User u = em.find(User.class, userId);
            ChallengeUser chUs = new ChallengeUser();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dc = sdf.parse(dateCreated);

            chUs.setUser(u);
            chUs.setChallenge(c);
            chUs.setDateCreated(dc.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            super.create(chUs);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    

    @PUT
    @Path("completeChallenge/{id}/{date}/{score}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ChallengeUser completeChallenge(@PathParam("id") Long id, @PathParam("date") String compDate, @PathParam("score") Long score) {
        try{
            ChallengeUser ch = em.find(ChallengeUser.class, id);
            
            if(ch.getDateCompleted() == null){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date cd = sdf.parse(compDate);
                

                ch.setDateCompleted(cd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                ch.setScore(score);
            } else {
                ch.setDateCompleted(null);
                ch.setScore(new Long(0));
            }
            super.edit(ch);
            return ch;
        }catch(Exception e){
            e.printStackTrace();
            return null;

        }
    }
    
    @PUT
    @Path("completeCreateChallenge")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response completeCreateChallenge(ChallengeUser entity) {
        try{
            Challenge c = em.find(Challenge.class, entity.getChallenge().getId());
            List<ChallengeUser> chList = 
                    em.createQuery("SELECT ch FROM ChallengeUser ch "
                            + "WHERE ch.user.id=:userId AND ch.challenge.id=:challengeId "
                            + "ORDER BY ch.dateCompleted DESC")
                    .setParameter("userId", entity.getUser().getId())
                    .setParameter("challengeId", entity.getChallenge().getId())
                    .getResultList();
            
            Challenge.ChallengeType ct = c.getType();
            
            if(chList.isEmpty()){
                ChallengeUser newEntity = super.create(entity);
                return Response.ok(newEntity).build();
            } else {
                if(ct.equals( Challenge.ChallengeType.ONCE )){
                    return Response.status(Response.Status.NOT_MODIFIED).build();
                } else if(ct.equals( Challenge.ChallengeType.DAILY )) {
                    if(!chList.get(0).getDateCompleted().equals(entity.getDateCompleted())){
                        ChallengeUser newEntity = super.create(entity);
                        return Response.ok(newEntity).build();
                    }
                }
            }
            return Response.status(Response.Status.NOT_MODIFIED).build();
        }catch(Exception e){
            e.printStackTrace();
            return null;

        }
    }
    
    @DELETE
    @Path("deleteChallenge/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteChallenge(@PathParam("id") Long id) {
        try{
            super.remove(super.find(id));
            return Response.ok().build();
            /*
            Challenge c = em.find(Challenge.class, entity.getChallenge().getId());
            List<ChallengeUser> chList = 
                    em.createQuery("SELECT ch FROM ChallengeUser ch "
                            + "WHERE ch.user.id=:userId AND ch.challenge.id=:challengeId "
                            + "ORDER BY ch.dateCompleted DESC")
                    .setParameter("userId", entity.getUser().getId())
                    .setParameter("challengeId", entity.getChallenge().getId())
                    .getResultList();
            
            Challenge.ChallengeType ct = c.getType();
            
            if(chList.isEmpty()){
                ChallengeUser newEntity = super.create(entity);
                return Response.ok(newEntity).build();
            } else {
                if(ct.equals( Challenge.ChallengeType.ONCE )){
                    return Response.status(Response.Status.NOT_MODIFIED).build();
                } else if(ct.equals( Challenge.ChallengeType.DAILY )) {
                    if(!chList.get(0).getDateCompleted().equals(entity.getDateCompleted())){
                        ChallengeUser newEntity = super.create(entity);
                        return Response.ok(newEntity).build();
                    }
                }
            }
            return Response.status(Response.Status.NOT_MODIFIED).build();
            */
        }catch(Exception e){
            e.printStackTrace();
            return null;

        }
    }
    
    @GET
    @Path("find/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ChallengeUser> findByUser(@PathParam("userId") String uId) {
        try {
            List<ChallengeUser> l = getEntityManager().createQuery("SELECT tu FROM ChallengeUser tu WHERE tu.user.id=:userId")
                .setParameter("userId", Long.parseLong(uId))
                .getResultList();
            if(l.isEmpty()) {
                return Collections.emptyList();
            } else {
                return l;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ChallengeUser> findAll() {
        return super.findAll();
    }
    
    @GET
    @Path("points")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String sumUserPoints(){
        String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        try {
        return  getEntityManager().createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.user.email=:email")
                .setParameter("email", userEmail)
                .getSingleResult().toString();
        } catch (Exception e) {
            return "0";
        }
    }
    
    @GET
    @Path("sent/{startDate}/{endDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ChallengeUser> findBySentDate(@PathParam("startDate") String sd, @PathParam("endDate") String ed) {
        try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(sd);   
        Date endDate = sdf.parse(ed);

        String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        
        return getEntityManager().createQuery("SELECT c FROM ChallengeUser c WHERE c.user.email=:email AND (c.dateCreated BETWEEN :start AND :end)")
                .setParameter("email", userEmail)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    @GET
    @Path("completed/{startDate}/{endDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ChallengeUser> findByCompletedDate(@PathParam("startDate") String sd, @PathParam("endDate") String ed) {
        try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(sd);   
        Date endDate = sdf.parse(ed);

        String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        
        return getEntityManager().createQuery("SELECT c FROM ChallengeUser c WHERE c.user.email=:email AND (c.dateCompleted BETWEEN :start AND :end)")
                .setParameter("email", userEmail)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    @GET
    @Path("rankFromDate")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ChallengeUserController.NicknameScore> rankFromDate(@PathParam("startDate") String sd) {
        try {
            List<ChallengeUserController.NicknameScore> resultList = new LinkedList<>();
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate dateStart = sdf.parse(sd).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();   

            List<User> users = this.getEntityManager()
                                .createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
                                .getResultList();

            users.forEach(u -> {
                long points = challengeUserController.getPointsFromDate(u, dateStart);
                resultList.add(new ChallengeUserController.NicknameScore(u.getNickname(), points));
            });

            return resultList;

        } catch (ParseException ex) {
            return null;
        }
    }
    
    
    
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
