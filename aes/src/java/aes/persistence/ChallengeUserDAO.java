/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.controller.ChallengeUserController;
import aes.model.Challenge;
import aes.model.ChallengeUser;
import aes.model.User;
import aes.service.ChallengeUserFacadeREST;
import com.google.gson.Gson;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author patri
 */
public class ChallengeUserDAO extends GenericDAO<ChallengeUser>{

    public ChallengeUserDAO() throws NamingException {
        super(ChallengeUser.class);
    }
    
    

    /*public Response completeCreateChallenge(ChallengeUser entity, EntityManager entityManager) {
        try {
            Challenge c = entityManager.find(Challenge.class, entity.getChallenge().getId());
            List<ChallengeUser> chList
                    = entityManager.createQuery("SELECT ch FROM ChallengeUser ch "
                            + "WHERE ch.user.id=:userId AND ch.challenge.id=:challengeId "
                            + "ORDER BY ch.dateCompleted DESC")
                            .setParameter("userId", entity.getUser().getId())
                            .setParameter("challengeId", entity.getChallenge().getId())
                            .getResultList();

            Challenge.ChallengeType ct = c.getType();

            if (chList.isEmpty()) {
                super.insert(entity, entityManager);
                //return Response.ok(newEntity).build();
            } else {
                if (ct.equals(Challenge.ChallengeType.ONCE)) {
                    return Response.status(Response.Status.NOT_MODIFIED).build();
                } else if (ct.equals(Challenge.ChallengeType.DAILY)) {
                    if (!chList.get(0).getDateCompleted().equals(entity.getDateCompleted())) {
                        ChallengeUser newEntity = super.create(entity);
                        return Response.ok(newEntity).build();
                    }
                }
            }
            return Response.status(Response.Status.NOT_MODIFIED).build();
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();

        }
    }*/


    /*public Response deleteChallenge(Long id) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();
            ChallengeUser chUs = super.find(id);
            if (chUs.getUser().getEmail().equals(userEmail)) {
                super.remove(super.find(id));
                return Response.ok().build();
            } else {
                return Response.notModified().build();
            }

        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }*/


    public List<ChallengeUser> findByUser(String uId, String userEmail, EntityManager entityManager) {
     
            //String userEmail = securityContext.getUserPrincipal().getName();

            List<ChallengeUser> l = entityManager.createQuery("SELECT tu FROM ChallengeUser tu WHERE tu.user.id=:userId AND tu.user.email=:userEmail")
                    .setParameter("userId", Long.parseLong(uId))
                    .setParameter("userEmail", userEmail)
                    .getResultList();
            /*if (l.isEmpty()) {
                return Response.ok().entity(Collections.emptyList()).build();
            } else {
                return Response.ok().entity(l).build();
            }*/
            
            return l;
     
    }


    public String sumUserPoints(String userEmail, EntityManager entityManager) {
        //String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        try {
            return entityManager.createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.user.email=:email")
                    .setParameter("email", userEmail)
                    .getSingleResult().toString();
        } catch (Exception e) {
            return "0";
        }
    }


    public List<ChallengeUser> findBySentDate(String sd, String ed, String userEmail, EntityManager entityManager) throws ParseException {
    
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(sd);
            Date endDate = sdf.parse(ed);

            //String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();

            List<ChallengeUser> list = entityManager.createQuery("SELECT c FROM ChallengeUser c WHERE c.user.email=:email AND (c.dateCreated BETWEEN :start AND :end)")
                    .setParameter("email", userEmail)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
            return list;

    }


    public List<ChallengeUser> findByCompletedDate(String sd,  String ed, String userEmail, EntityManager entityManager) throws ParseException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(sd);
            Date endDate = sdf.parse(ed);

            //String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();

            List<ChallengeUser> list = entityManager.createQuery("SELECT c FROM ChallengeUser c WHERE c.user.email=:email AND (c.dateCompleted BETWEEN :start AND :end)")
                    .setParameter("email", userEmail)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
            
            return list;

    }


    public List<ChallengeUserController.NicknameScore> rankFromDate(String sd, EntityManager entityManager) throws ParseException {
            List<ChallengeUserController.NicknameScore> resultList = new LinkedList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate dateStart = sdf.parse(sd).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<User> users = entityManager
                    .createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
                    .getResultList();

            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart, entityManager);
                resultList.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            return resultList;


    }

    public static class RankLists {

        List<ChallengeUserController.NicknameScore> weeklyResult;
        List<ChallengeUserController.NicknameScore> monthlyResult;
        List<ChallengeUserController.NicknameScore> yearlyResult;

        public RankLists() {
            weeklyResult = new LinkedList<>();
            monthlyResult = new LinkedList<>();
            yearlyResult = new LinkedList<>();
        }
    }

    public RankLists rank(String today, EntityManager entityManager) throws ParseException {

            RankLists rank = new RankLists();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate dateStart = sdf.parse(today).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
                    .getResultList();

            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart.with(ChronoField.DAY_OF_WEEK, 1), entityManager);
                rank.weeklyResult.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart.with(ChronoField.DAY_OF_MONTH, 1), entityManager);
                rank.monthlyResult.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart.with(ChronoField.DAY_OF_YEAR, 1), entityManager);
                rank.yearlyResult.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });

           return rank;


    }



    protected long getPointsFromDate(User u, LocalDate date, EntityManager entityManager) {
        Long score = (Long) entityManager
                .createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.dateCompleted > :date AND c.user.id=:userId")
                .setParameter("date", date)
                .setParameter("userId", u.getId()).getSingleResult();
        if (score == null) {
            score = Long.valueOf(0);
        }
        return score;
    }
    
    public long countByUserAndChallenge(Long userId, Long challengeId, EntityManager entityManager) {
        try {
            return (long) entityManager.createQuery("SELECT COUNT(cu) FROM ChallengeUser cu " +
                    "WHERE cu.user.id = :userId AND cu.challenge.id = :challengeId")
                    .setParameter("userId", userId)
                    .setParameter("challengeId", challengeId)
                    .getSingleResult();
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserDAO.class.getName()).log(Level.SEVERE, null, e);
            return 0L;
        }
    }
}
