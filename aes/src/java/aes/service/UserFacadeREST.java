/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.controller.ContactController;
import aes.controller.UserController;
import aes.model.User;
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
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + entity.getEmail() + "'cadastrou no sistema.");

                emailHelper.sendSignUpEmail(entity, em);
                if (entity.isReceiveEmails()) {
                    contactDAO.scheduleTipsEmail(entity);
                    contactDAO.scheduleDiaryReminderEmail(entity, new Date());
                    contactDAO.scheduleWeeklyEmail(entity, new Date());
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
    
    /*
    @POST
    @Path("createUnregistered")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUnregistered(User entity) {
        List<User> userList = em.createQuery("SELECT u FROM User u WHERE u.email=:e").setParameter("e", entity.getEmail()).getResultList();
        try {
            if(!userList.isEmpty()){

                if(userList.get(0).isRegistration_complete()){
                    String error = "{\"error\": \"EmailInUseLogin\"}";
                    return Response.status(Response.Status.CONFLICT).entity(error).build();
                } else {
                    entity.setId(userList.get(0).getId());
                    userTransaction.begin();
                    super.edit(entity);
                    userTransaction.commit();
                }

            } else {
                userTransaction.begin();
                super.create(entity);
                userTransaction.commit();
            }
            return Response.ok().entity(entity).build();

        } catch(Exception e){
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    */
    
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
        System.out.println("aes.service.UserFacadeREST.setInRanking()");
        try{
            
            userDAO.setInRanking(userEmail, em);      
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
            
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.INFO, null, "Recover password service");

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

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
