/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.controller.ContactController;
import aes.controller.UserController;
import aes.model.AgendaAppointment;
import aes.model.User;
import aes.persistence.AgendaAppointmentDAO;
import aes.persistence.ContactDAO;
import aes.persistence.UserDAO;
import aes.utility.EmailHelper;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import aes.utility.Secured;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
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
@TransactionManagement(TransactionManagementType.BEAN)
@Path("secured/user")
public class UserFacadeREST extends AbstractFacade<User> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private UserDAO userDAO;
    private ContactDAO contactDAO;
    private AgendaAppointmentDAO appointmentDao;
    private EmailHelper emailHelper;
    
    @Inject
    private ContactController contactController;
    
    @Inject
    private UserController userController;
    
    @Resource
    private UserTransaction userTransaction;
    

    @Context
    SecurityContext securityContext;
    
    public UserFacadeREST(){
        super(User.class);
        emailHelper = new EmailHelper();
        try {
            userDAO = new UserDAO();
            contactDAO = new ContactDAO();
            appointmentDao = new AgendaAppointmentDAO();
        } catch (NamingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{password}")
    public Response createUser(User entity, @PathParam("password") String p) {
        List<User> userList = em.createQuery("SELECT u FROM User u WHERE u.email=:e").setParameter("e", entity.getEmail()).getResultList();
        
        if (!userList.isEmpty()) {
            return Response.status(Response.Status.CONFLICT).build();
        } else {
            try {
                String clientEncriptedHexPassword = p;
                String decriptedPassword = Encrypter.decrypt(clientEncriptedHexPassword);
                
                userDAO.createUser(entity, decriptedPassword, em);
                
                /*byte[] salt =  Encrypter.generateRandomSecureSalt(16);
                entity.setSalt(salt);
                entity.setPassword(Encrypter.hashPassword(decriptedPassword, salt));
                
                userTransaction.begin();
                super.create(entity);
                userTransaction.commit();*/
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Usuário '" + entity.getEmail() + "'cadastrou no sistema.");

                emailHelper.sendSignUpEmail(entity, em);
                if (entity.isReceiveEmails()) {
                    contactDAO.scheduleTipsEmail(entity, em);
                    contactDAO.scheduleDiaryReminderEmail(entity, new Date(), em);
                    contactDAO.scheduleWeeklyEmail(entity, new Date(), em);
                }
            
            
            return Response.ok(entity).build();
            
             } catch (SQLException | EncrypterException ex) {
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                return Response.serverError().build();

            }catch( MessagingException | MissingResourceException ex){
                 Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                 return Response.ok(entity).build();
            }
        }
        
         
        // return Response.serverError().build();
        
    }
        
    @PUT
    @Path("/toggleConsultant/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleConsultant(@PathParam("id") Long id) {
        String userEmail = securityContext.getUserPrincipal().getName();
        try{
            
            userDAO.toggleConsultant(userEmail, em);
            /*User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                                .setParameter("email", userEmail)
                                .getSingleResult();
            u.setConsultant(!u.isConsultant());
            userTransaction.begin();
            super.edit(u);
            userTransaction.commit();*/
            return Response.status(Response.Status.NO_CONTENT).build();
        }catch(Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @PUT
    @Path("/setInRanking")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setInRanking(User entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        try{
            userDAO.setInRanking(userEmail,entity.getInRanking(),entity.getNickname(), em);
            System.out.println("aes.service.UserFacadeREST.setInRanking()");
           return Response.status(Response.Status.NO_CONTENT).build();

        }catch(Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();

        }
    }
    
    @PUT
    @Path("/sendTCLE")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendTCLE(User entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        try{
            userDAO.setSendTCLE(userEmail, entity, em);
            System.out.println("aes.service.UserFacadeREST.setSendTCLE()");
            return Response.status(Response.Status.NO_CONTENT).build();

        }catch(Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    public static class EmailClass {
        public String email;
        public EmailClass(String email){
            this.email = email;
        }
    }
    
    @PUT
    @Path("recover-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recoverPassword(JsonParser jp) {
        try {
            JsonNode node = jp.getCodec().readTree(jp);
            String userEmail = node.get("email").asText();
            System.out.println("aes.service.UserFacadeREST.forgetPassword()");
            /*User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                    .setParameter("email", userEmail)
                    .getSingleResult();
            System.out.println(u.getEmail());
            u.setRecoverCode(GenerateCode.generate());

            userTransaction.begin();
            super.edit(u);
            userTransaction.commit();*/
            
            User u = userDAO.generateRecoverCode(userEmail, em);
            emailHelper.sendPasswordRecoveryEmail(u, em);
            
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, "Recover password service");

            return Response.ok().build();
        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @PUT
    @Path("deleteAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAccount(JsonParser jp) {
        try {
            JsonNode node = jp.getCodec().readTree(jp);
            String userId = node.get("id").asText();
            String token = node.get("token").asText();
            User u = (User) em.createQuery("SELECT u FROM User u WHERE u.id=:userId")
                .setParameter("userId", Long.parseLong(userId))
                .getSingleResult();
            System.out.println("aes.service.UserFacadeREST.deleteAccount()");
            emailHelper.sendDeleteAccountEmail(u,em,token);
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, "Delete Account service");

            return Response.ok().build();
        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("count")
    @Secured
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @GET
    @Secured
    @Path("login/{token}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Override
    public User login(@PathParam("token") String tkn) {
        return super.login(tkn);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("findUserByNickname/{nickname}")
    public Response findUserByNickname(@PathParam("nickname") String nickname) {
        try {
            User userResult = (User) em.createQuery("SELECT u FROM User u WHERE u.nickname=:nm")
                .setParameter("nm", nickname)
                .getSingleResult();
        
            if (userResult == null) {
                return Response.status(Response.Status.CONFLICT).build();
            } else {           
                return Response.ok().entity(userResult).build();
            }    
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @PUT
    @Path("/changeTitle/")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeTitle(User entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        try{
            userDAO.setTitle(userEmail, entity, em);
            System.out.println("aes.service.UserFacadeREST.setTitle()");
            return Response.status(Response.Status.NO_CONTENT).build();

        }catch(Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @PUT
    @Path("changeUserConsultant/{userId}/{consultantId}/{adminEmail}/{adminPassword}")
    @Secured
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response changeUserConsultant(
            @PathParam("userId") Long userId, 
            @PathParam("consultantId") Long consultantId, 
            @PathParam("adminEmail") String adminEmail,
            @PathParam("adminPassword") String adminPassword
    ) {
        User user;
        User newConsultant;
        User admin;
        try {
            admin = userDAO.checkCredentials(adminEmail, adminPassword, em);
            if(!Objects.equals(admin, null) && admin.isAdmin()) {
                if(!userDAO.find(userId, em).isConsultant() && userDAO.find(consultantId, em).isConsultant()) {
                    user = userDAO.find(userId, em);
                    newConsultant = userDAO.find(consultantId, em);
                    List<AgendaAppointment> userCurrentAppointments = appointmentDao.listCurrentByUser(userId, em);
                    for(AgendaAppointment appointment : userCurrentAppointments) {
                        appointmentDao.delete(appointment, em);
                    }
                    user.setRelatedConsultant(newConsultant);
                    userDAO.update(user, em);
                    return Response.status(Response.Status.OK).build();
                }
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (SQLException | RuntimeException | EncrypterException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("/updateEvaluationProfile/")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvaluationProfile(User entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        try {
            userDAO.updateEvaluationProfile(userEmail, entity, em);
            System.out.println("aes.service.UserFacadeREST.updateEvaluationProfile()");
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("findUserByChatId/{chatId}")
    @Secured
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findUserByChatId(@PathParam("chatId") Long chatId) {
        try {
            return Response.ok().entity(userDAO.listOnce("chat.id", chatId, em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
