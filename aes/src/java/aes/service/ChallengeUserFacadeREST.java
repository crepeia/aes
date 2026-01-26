package aes.service;

import aes.controller.ChallengeUserController;
import aes.model.ChallengeUser;
import aes.model.User;
import aes.persistence.ChallengeUserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
@TransactionManagement(TransactionManagementType.BEAN)
public class ChallengeUserFacadeREST extends AbstractFacade<ChallengeUser> {
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    @Inject
    private SecurityContextHelper securityHelper;
    
    @Context
    SecurityContext securityContext;
    
    private ChallengeUserDAO challengeUserDao;

    public ChallengeUserFacadeREST() {
        super(ChallengeUser.class);
        try {
            challengeUserDao = new ChallengeUserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
        }
    }
    
    public class ChallengeUserDTO {
        public long id;
        public Long challengeId;
        public LocalDate dateCreated;
        public LocalDate dateCompleted;
        public Long score;
    };
    
    public static class RankLists {
        List<ChallengeUserController.NicknameScore> weeklyResult;
        List<ChallengeUserController.NicknameScore> monthlyResult;
        List<ChallengeUserController.NicknameScore> yearlyResult;

        public RankLists() {
            weeklyResult = new LinkedList<>();
            monthlyResult = new LinkedList<>();
            yearlyResult = new LinkedList<>();
        }
    };
    
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
    
    @PUT
    @Path("completeCreateChallenge")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response completeCreateChallenge(ChallengeUser entity) throws SQLException {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null || entity.getChallengeId() == null || entity.getScore() == null) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_COMPLETE_CREATE_CHALLENGE reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            Properties properties = new Properties();
            String propertiesPath = this.getMessagesPath() + "_pt.properties";
            InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesPath);
            
            if (input == null) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.SEVERE, "Properties file not found: {0}", propertiesPath);
                
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
            }
            
            properties.load(input);
            
            List<ChallengeUser> chList = challengeUserDao.findAllChallengesUserOrdered(loggedUser.getId(), entity.getChallengeId(), em);
            
            if (chList.isEmpty()) {
                ChallengeUser newEntity = new ChallengeUser();
                newEntity.setUser(loggedUser);
                newEntity.setChallengeId(entity.getChallengeId());
                newEntity.setDateCreated(LocalDate.now());
                newEntity.setDateCompleted(LocalDate.now());
                newEntity.setScore(entity.getScore());
 
                challengeUserDao.insert(newEntity, em);
                
                ChallengeUserDTO dto = new ChallengeUserDTO();
                dto.id = newEntity.getId();
                dto.challengeId = newEntity.getChallengeId();
                dto.dateCreated = newEntity.getDateCreated();
                dto.dateCompleted = newEntity.getDateCompleted();
                dto.score = newEntity.getScore();
                
                return Response.status(Response.Status.CREATED).entity(dto).build();
            }
            
            String challengeType = properties.getProperty("challenge.type." + entity.getChallengeId());
            
            if (challengeType == null || challengeType.isEmpty()) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_COMPLETE_CREATE_CHALLENGE reason=INVALID_CHALLENGE_TYPE"
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            switch (challengeType) {
                case "ONCE":
                    return Response.status(Response.Status.NOT_MODIFIED).build();
                    
                case "DAILY":
                    LocalDate lastCompleted = chList.get(0).getDateCompleted();
                    LocalDate today = LocalDate.now();
                    if (lastCompleted == null || lastCompleted.isBefore(LocalDate.now())) {
                        ChallengeUser newEntity = new ChallengeUser();
                        newEntity.setUser(loggedUser);
                        newEntity.setChallengeId(entity.getChallengeId());
                        newEntity.setDateCreated(LocalDate.now());
                        newEntity.setDateCompleted(LocalDate.now());
                        newEntity.setScore(entity.getScore());

                        challengeUserDao.insert(newEntity, em);
                        
                        ChallengeUserDTO dto = new ChallengeUserDTO();
                        dto.id = newEntity.getId();
                        dto.challengeId = newEntity.getChallengeId();
                        dto.dateCreated = newEntity.getDateCreated();
                        dto.dateCompleted = newEntity.getDateCompleted();
                        dto.score = newEntity.getScore();

                        return Response.status(Response.Status.CREATED).entity(dto).build();
                    }
                    break;
                    
                case "ACCUMULATIVE":
                    // It always creates a new record, regardless of the date
                    ChallengeUser newEntity = new ChallengeUser();
                    newEntity.setUser(loggedUser);
                    newEntity.setChallengeId(entity.getChallengeId());
                    newEntity.setDateCreated(LocalDate.now());
                    newEntity.setDateCompleted(LocalDate.now());
                    newEntity.setScore(entity.getScore());

                    challengeUserDao.insert(newEntity, em);
                    
                    ChallengeUserDTO dto = new ChallengeUserDTO();
                    dto.id = newEntity.getId();
                    dto.challengeId = newEntity.getChallengeId();
                    dto.dateCreated = newEntity.getDateCreated();
                    dto.dateCompleted = newEntity.getDateCompleted();
                    dto.score = newEntity.getScore();
                    
                    return Response.status(Response.Status.CREATED).entity(dto).build();
                    
                default:
                    Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                        .log(Level.WARNING, "[SECURITY] DENIED_COMPLETE_CREATE_CHALLENGE reason=INVALID_CHALLENGE_TYPE "
                        + "loggedUserId={0}", loggedUser.getId());
                    
                    return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            return Response.status(Response.Status.NOT_MODIFIED).build();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @DELETE
    @Path("deleteChallenge/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteChallenge(@PathParam("id") Long challengeId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (challengeId == null) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_DELETE_CHALLENGE reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            ChallengeUser ch = challengeUserDao.find(challengeId, em);
            
            if (ch == null) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_DELETE_CHALLENGE reason=TARGET_OBJECT_NOT_FOUND "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            r = securityHelper.requireSameUser(loggedUser, ch.getUser().getId());
            if (r != null) return r;
            
            challengeUserDao.delete(ch, em);
            
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @GET
    @Path("findByUser")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByUser() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<ChallengeUser> l_ch = challengeUserDao.findByUser(loggedUser.getId(), em);
            
            if (l_ch == null) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.INFO, "[INFO] EMPTY_LIST_CHALLENGE_USER "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.OK).entity(Collections.emptyList()).build();
            }
            
            return Response.status(Response.Status.OK).entity(l_ch).build();
        } catch (Exception ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @GET
    @Path("points")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response sumUserPoints() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            String sum = challengeUserDao.sumUserPoints(loggedUser.getId(), em);
            
            if (sum == null || sum.isEmpty()) {
                return Response.status(Response.Status.OK).entity("0").build();
            }
            
            return Response.status(Response.Status.OK).entity(sum).build();
        } catch (Exception ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("points/{userId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserTotalPoints(@PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (userId == null) {
                Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_GET_TOTAL_POINTS reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            Logger.getLogger(ChallengeUserFacadeREST.class.getName())
                .log(Level.INFO, "[INFO] LOGGED_USER_ASKING_FOR_TOTAL_POINTS_OF_ANOTHER_USER "
                + "loggedUserId={0} paramUserId={1}", new Object[]{loggedUser.getId(), userId});
            
            String sum = challengeUserDao.sumUserPoints(userId, em);
            
            if (sum == null || sum.isEmpty()) {
                return Response.status(Response.Status.OK).entity("0").build();
            }
            
            return Response.status(Response.Status.OK).entity(sum).build();
        } catch (Exception ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    /*
    @GET
    @Path("sent/{startDate}/{endDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findBySentDate(@PathParam("startDate") String sd, @PathParam("endDate") String ed) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(sd);
            Date endDate = sdf.parse(ed);

            String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();

            List<ChallengeUser> list = getEntityManager().createQuery("SELECT c FROM ChallengeUser c WHERE c.user.email=:email AND (c.dateCreated BETWEEN :start AND :end)")
                    .setParameter("email", userEmail)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("completed/{startDate}/{endDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByCompletedDate(@PathParam("startDate") String sd, @PathParam("endDate") String ed) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(sd);
            Date endDate = sdf.parse(ed);

            String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();

            List<ChallengeUser> list = getEntityManager().createQuery("SELECT c FROM ChallengeUser c WHERE c.user.email=:email AND (c.dateCompleted BETWEEN :start AND :end)")
                    .setParameter("email", userEmail)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("rankFromDate")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response rankFromDate(@PathParam("startDate") String sd) {
        try {
            List<ChallengeUserController.NicknameScore> resultList = new LinkedList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate dateStart = sdf.parse(sd).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<User> users = getEntityManager()
                    .createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
                    .getResultList();

            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart);
                resultList.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            return Response.ok().entity(resultList).build();

        } catch (ParseException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }
    */

    @GET
    @Path("rankForToday")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response rank() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            LocalDate dateStart = LocalDate.now(ZoneId.systemDefault());

            List<User> users = challengeUserDao.getUsersInRank(em);
            
            RankLists rank = new RankLists();

            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart.with(ChronoField.DAY_OF_WEEK, 1));
                rank.weeklyResult.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart.with(ChronoField.DAY_OF_MONTH, 1));
                rank.monthlyResult.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            users.forEach(u -> {
                long points = getPointsFromDate(u, dateStart.with(ChronoField.DAY_OF_YEAR, 1));
                rank.yearlyResult.add(new ChallengeUserController.NicknameScore(u.getId(), u.getNickname(), points, u.getSelected_title()));
            });
            
            Gson g = new Gson();
            String json = g.toJson(rank);

            return Response.status(Response.Status.OK).entity(json).build();
        } catch (Exception ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    /*
    @GET
    @Path("countByUserAndChallenge/{userId}/{challengeId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response countByUserAndChallenge(
            @PathParam("userId") Long userId,
            @PathParam("challengeId") Long challengeId) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();

            // Verifica se o userId corresponde ao usuário logado;
            User user = em.find(User.class, userId);
            if (user == null || !user.getEmail().equals(userEmail)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            long count = (long) em.createQuery("SELECT COUNT(cu) FROM ChallengeUser cu " +
                    "WHERE cu.user.id = :userId AND cu.challengeId = :challengeId")
                    .setParameter("userId", userId)
                    .setParameter("challengeId", challengeId)
                    .getSingleResult();

            return Response.ok(String.valueOf(count)).build();
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /*
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }
    */
}
