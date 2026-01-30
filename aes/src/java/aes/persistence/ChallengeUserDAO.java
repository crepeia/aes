package aes.persistence;

import aes.controller.ChallengeUserController;
import aes.model.Challenge;
import aes.model.ChallengeUser;
import aes.model.User;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class ChallengeUserDAO extends GenericDAO<ChallengeUser>{

    public ChallengeUserDAO() throws NamingException {
        super(ChallengeUser.class);
    }
    
    public List<ChallengeUser> findAllChallengesUserOrdered(Long userId, Long challengeId, EntityManager entityManager) {
        return (List<ChallengeUser>) entityManager.createQuery(
            "SELECT ch FROM ChallengeUser ch WHERE ch.user.id =: userId AND ch.challengeId =: challengeId ORDER BY ch.dateCompleted DESC")
                .setParameter("userId", userId)
                .setParameter("challengeId", challengeId)
                .getResultList();
    }
    
    public List<ChallengeUser> findByUser(Long userId, EntityManager entityManager) {
        return (List<ChallengeUser>) entityManager.createQuery("SELECT ch FROM ChallengeUser ch WHERE ch.user.id =: userId")
            .setParameter("userId", userId)
            .getResultList();
    }

    public String sumUserPoints(Long userId, EntityManager entityManager) {
        return (String) entityManager.createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.user.id =: userId")
            .setParameter("userId", userId)
            .getSingleResult().toString();
    }

    public List<User> getUsersInRank(EntityManager entityManager) {
        return (List<User>) entityManager.createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
            .getResultList();
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
