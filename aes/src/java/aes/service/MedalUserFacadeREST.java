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
import aes.persistence.UserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.time.LocalDate;
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
 * @author Matheus Carvalho
 */
@Stateless
@Path("medaluser")
@Secured
@TransactionManagement(TransactionManagementType.BEAN)
public class MedalUserFacadeREST extends AbstractFacade<MedalUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private MedalUserDAO medalUserDao;
    private UserDAO userDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public MedalUserFacadeREST() {
        super(MedalUser.class);
        try {
            medalUserDao = new MedalUserDAO();
            userDao = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("find")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByUser() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<MedalUser> list = medalUserDao.findByUserId(loggedUser.getId(), em);
            
            if (list.isEmpty()) {
                Logger.getLogger(MedalUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DID_NOT_FIND_MEDALS reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                    
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            return Response.ok().entity(list).build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMedal(MedalUser entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null || entity.getMedal() == null) {
                Logger.getLogger(MedalUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_MEDAL_USER reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            List<MedalUser> existingList =
                medalUserDao.findByUserMedalAndDescription(
                        loggedUser.getId(),
                        entity.getMedal().getId(),
                        entity.getDescription(),
                        em);
            
            if (existingList.isEmpty()) {
                entity.setUser(loggedUser);
                super.create(entity);
            }
            
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("createInitialMedalAA")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createInitialMedalAA(MedalUser entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null || entity.getMedal() == null) {
                Logger.getLogger(MedalUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_MEDAL_USER reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            List<MedalUser> existingList =
                medalUserDao.findByUserAndMedal(
                        loggedUser.getId(),
                        entity.getMedal().getId(),
                        em);
            
            if (existingList.isEmpty()) {
                entity.setUser(loggedUser);
                super.create(entity);
            }
            
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("updateMedal")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMedal(MedalUser entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null || entity.getMedal() == null) {
                Logger.getLogger(MedalUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_MEDAL_USER reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            MedalUser mu = medalUserDao.findSingleResultByUserAndMedal(loggedUser.getId(), entity.getMedal().getId(), em);
            
            if (mu == null) {
                Logger.getLogger(MedalUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_MEDAL_UPDATE reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());

                return Response.status(Response.Status.NOT_FOUND)
                        .entity("TARGET_OBJECT_NOT_FOUND")
                        .build();
            }
            
            mu.setDescription(entity.getDescription());
            MedalUser newEntity = super.edit(mu);
            return Response.ok().build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("getMonthlyDrinkMedal")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyDrinkMedal() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            LocalDate today = LocalDate.now();
            int month = today.getMonthValue();
            String result = medalUserDao.getMonthlyDrinkMedalByUser(loggedUser.getId(), today, month, em);
            
            if (result == null) {
                return Response.ok().entity("0").build();
            }
            
            return Response.ok().entity(result).build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("getMonthlyNotDrinkMedal")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyNotDrinkMedal() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            LocalDate today = LocalDate.now();
            int month = today.getMonthValue();
            String result = medalUserDao.getMonthlyNotDrinkMedalByUser(loggedUser.getId(), today, month, em);
            
            if (result == null) {
                return Response.ok().entity("0").build();
            }
            
            return Response.ok().entity(result).build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }


    @GET
    @Path("getDrinkLog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrinkLog() {        
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<DailyLog> list = medalUserDao.getDrinkLogByUser(loggedUser.getId(), em);

            return Response.ok().entity(list).build();
        } catch (Exception ex) {
            Logger.getLogger(MedalUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
}
