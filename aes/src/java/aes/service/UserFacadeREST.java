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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
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
    
    public class AnonymousUserDTO {
        public long id;
        public String unauthenticatedId;
        public String name;
        public Date signUpDate;
        public String preferedLanguage;
        public Date dateCreated;
        public String ipCreated;
        public Boolean app_signup;
        public Boolean registration_complete;
    };
    
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
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @Path("/createAnonymousUser")
    public Response createAnonymousUser(User entity) throws SQLException {
        try {
            User existingUser = null;
            try {
                existingUser = em.createQuery(
                    "SELECT u FROM User u Where u.unauthenticatedId = :id ORDER BY u.id DESC", User.class)
                    .setParameter("id", entity.getUnauthenticatedId())
                    .setMaxResults(1)
                    .getSingleResult();
            } catch (NoResultException e) {
                // Nenhum usuário encontrado — segue para criação
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Usuário Anônimo cadastrou no sistema.");
            }
            
            if (existingUser != null) {
                return Response.ok(existingUser).build();
            }
            
            userDAO.createAnonymousUser(entity, em);
            
            AnonymousUserDTO dto = new AnonymousUserDTO();
            dto.id = entity.getId();
            dto.unauthenticatedId = entity.getUnauthenticatedId();
            dto.name = entity.getName();
            dto.signUpDate = entity.getSignUpDate();
            dto.preferedLanguage = entity.getPreferedLanguage();
            dto.dateCreated = entity.getDateCreated();
            dto.ipCreated = entity.getIpCreated();
            dto.app_signup = entity.isApp_signup();
            dto.registration_complete = true;
            
            return Response.ok(dto).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @Path("/recreateAnonymousUser")
    public Response recreateAnonymousUser(User entity) throws SQLException {
        try {
            // Sempre cria um novo user anônimo
            userDAO.createAnonymousUser(entity, em);

            AnonymousUserDTO dto = new AnonymousUserDTO();
            dto.id = entity.getId();
            dto.unauthenticatedId = entity.getUnauthenticatedId();
            dto.name = entity.getName();
            dto.signUpDate = entity.getSignUpDate();
            dto.preferedLanguage = entity.getPreferedLanguage();
            dto.dateCreated = entity.getDateCreated();
            dto.ipCreated = entity.getIpCreated();
            dto.app_signup = entity.isApp_signup();
            dto.registration_complete = true;

            return Response.ok(dto).build();

        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
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
        if(entity == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User entity cannot be null").build();
        }
        
        String userEmail = securityContext.getUserPrincipal().getName();
        try {
            //Teste
//            User user = new User();
//            user.setEducation(1);
//            user.setEmployed(true);
//            user.setKnowWebsite(1);
//            userDAO.updateEvaluationProfile(userEmail, user, em);
            userDAO.updateEvaluationProfile(userEmail, entity, em);
            return Response.status(Response.Status.OK).entity("User profile updated successfully").build();
        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
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
    
    @POST
    @Path("/validate-referral-code")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateReferralCode(String jsonInput) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonInput);
            String referralCode = node.get("referral_code").asText();

            User referrer = userDAO.findByReferralCode(referralCode, em);

            if (referrer == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"valid\":false, \"message\":\"Código inválido ou não encontrado\"}")
                    .build();
            }

            return Response.ok()
                .entity("{\"valid\":true, \"message\":\"Código válido\", \"referrerId\":" + 
                       referrer.getId() + "}")
                .build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"valid\":false, \"message\":\"Erro: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @POST
    @Path("/set-friend-referral-code")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setFriendReferralCode(String jsonInput) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonInput);

            if (!node.has("id") || !node.has("code")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"success\":false, \"message\":\"Campos 'id' e 'code' são obrigatórios\"}")
                    .build();
            }

            Long userId = node.get("id").asLong();
            String referralCode = node.get("code").asText();

            if (userId == null || referralCode == null || referralCode.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"success\":false, \"message\":\"ID inválido ou código vazio\"}")
                    .build();
            }

            userTransaction.begin();
            userDAO.updateReferralCode(userId, referralCode, em);
            userTransaction.commit();

            return Response.ok()
                .entity("{\"success\":true, \"message\":\"Código de referência atualizado com sucesso\"}")
                .build();

        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"success\":false, \"message\":\"JSON inválido\"}")
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"success\":false, \"message\":\"" + e.getMessage() + "\"}")
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"success\":false, \"message\":\"" + e.getMessage() + "\"}")
                .build();
        } catch (Exception e) {
            try {
                if (userTransaction != null) userTransaction.rollback();
            } catch (Exception ex) {
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao fazer rollback", ex);
            }

            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido - verifique logs";
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"success\":false, \"message\":\"" + errorMsg + "\"}")
                .build();
        }
    }
    
    @POST
    @Path("/count-referral-usage")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response countReferralCodeUsage(String jsonInput) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonInput);

            if (!node.has("referral_code")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"O campo 'referral_code' é obrigatório\"}")
                    .build();
            }

            String referralCode = node.get("referral_code").asText();
            long count = userDAO.countReferralCodeUsage(referralCode, em);

            return Response.ok()
                .entity("{\"count\":" + count + ", \"referral_code\":\"" + referralCode + "\"}")
                .build();

        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"JSON inválido\"}")
                .build();
        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao processar a requisição\"}")
                .build();
        }
    }
    
    @GET
    @Path("/get-referral-code/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserReferralCode(@PathParam("userId") Long userId) {
        try {
            // Validação básica do ID
            if (userId == null || userId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"ID de usuário inválido\"}")
                    .build();
            }

            // Busca o usuário no banco de dados
            User user = em.find(User.class, userId);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Usuário não encontrado\"}")
                    .build();
            }

            // Obtém o código de referência
            String referralCode = user.getMyReferralCode();

            // Se o usuário não tiver código cadastrado
            if (referralCode == null || referralCode.trim().isEmpty()) {
                return Response.ok()
                    .entity("{\"exists\":false, \"message\":\"Usuário não possui código de referência\"}")
                    .build();
            }

            // Retorna o código encontrado
            return Response.ok()
                .entity("{\"exists\":true, \"referral_code\":\"" + referralCode + "\"}")
                .build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao buscar código de referência\"}")
                .build();
        }
    }
    
    @PUT
    @Path("updateAppSignInDate/{timestamp}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAppSignInDate(@PathParam("timestamp") Long timestamp, @PathParam("userId") Long userId) throws ParseException {
        try {
            Date date = new Date(timestamp);
            User user = userDAO.find(userId, em);
            user.setAppSignInDate(date);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("updateAdmin/{isAdmin}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAdmin(@PathParam("isAdmin") boolean isAdmin, @PathParam("userId") Long userId) {
        try {
            User user = userDAO.find(userId, em);
            user.setAdmin(isAdmin);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("updateConsultant/{isConsultant}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConsultant(@PathParam("isConsultant") boolean isConsultant, @PathParam("userId") Long userId) {
        try {
            User user = userDAO.find(userId, em);
            user.setConsultant(isConsultant);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("updateUseChatbot/{useChatbot}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUseChatbot(@PathParam("useChatbot") boolean useChatbot, @PathParam("userId") Long userId) {
        try {
            User user = userDAO.find(userId, em);
            user.setUse_chatbot(useChatbot);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(User user) {
        try {
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    

    
    @GET
    @Path("listForAdmin/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllUsers(@PathParam("userId") Long userId) {
        try {
            User user = userDAO.find(userId, em);
            
            // Verifica se usuario eh administrador para retornar a lista de usuarios
            if (!user.isAdmin()) {
                // Se nao for administrador retorna UNAUTHORIZED
                return Response.status(Response.Status.UNAUTHORIZED).entity("Usuario nao autorizado").build();
            }
            
            List<User> users = userDAO.listNotNull("email", em);
            
            List<Map<String, Object>> usersDTO = users.stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("email", u.getEmail());
                    map.put("isAdmin", u.isAdmin());
                    map.put("isConsultant", u.isConsultant());
                    map.put("useChatbot", u.isUse_chatbot());
                    return map;
                })
                .collect(Collectors.toList());
            
            return Response.ok(usersDTO).build();
        } catch (SQLException e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao listar usuários", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("{\"error\":\"Erro ao buscar usuários\"}")
            .build();
        }
    }

    @GET
    @Path("/info/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@PathParam("id") Long id) {
        try {
            User user = em.find(User.class, id);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Usuário não encontrado.").build();
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getNickname());
            userInfo.put("birthDate", user.getBirthDate());
            userInfo.put("gender", user.getGender());

            return Response.ok(userInfo).build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erro ao buscar informações do usuário.").build();
        }
    }
    
    @GET
    @Path("/findUsersByConsultor/{consultantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByConsultor(@PathParam("consultantId") Long consultantId) {
        try {
            // Verifica se o consultor existe e é realmente um consultor
            User consultant = em.find(User.class, consultantId);
            if (consultant == null || !consultant.isConsultant()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Consultor não encontrado ou ID não pertence a um consultor\"}")
                    .build();
            }

            // Busca os usuários associados a este consultor usando o relacionamento mapeado
            List<User> clients = em.createQuery(
                "SELECT u FROM User u WHERE u.relatedConsultant.id = :consultantId", User.class)
                .setParameter("consultantId", consultantId)
                .getResultList();

            // Cria a lista de resposta com os campos relevantes
            List<Map<String, Object>> response = clients.stream()
                .map(client -> {
                    Map<String, Object> clientInfo = new HashMap<>();
                    clientInfo.put("_id", client.getId());
                    clientInfo.put("name", client.getName());
                    clientInfo.put("email", client.getEmail());
                    clientInfo.put("nickname", client.getNickname());
                    clientInfo.put("registration_complete", client.isRegistration_complete());
                    // Adiciona mais campos conforme necessário pelo frontend
                    return clientInfo;
                })
                .collect(Collectors.toList());

            return Response.ok(response).build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao buscar usuários do consultor", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao processar a requisição\"}")
                .build();
        }
    }
    
    @GET
    @Path("/findRelatedConsultantByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelatedConsultantByUser(@PathParam("userId") Long userId) {
        try {
            // Busca o usuário
            User user = em.find(User.class, userId);
            
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Usuário não encontrado.").build();
            }
            
            // Monta o JSON de resposta
            Map<String, Object> result = new HashMap<>();
            
            if (user.getRelatedConsultant() != null) {
                result.put("relatedConsultantId", user.getRelatedConsultant().getId());
            } else {
                result.put("relatedConsultantId", null);
            }
            
            return Response.ok(result).build();
        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao buscar o consultor relacionado.").build();
        }
    }
    
    @PUT
    @Path("/updateProfilePick/{id}/{profilePick}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfilePick(@PathParam("id") Long id, @PathParam("profilePick") Integer profilePick) {
        try {
            User user = em.find(User.class, id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            user.setProfilePick(profilePick);

            userTransaction.begin();
            em.merge(user);
            userTransaction.commit();

            return Response.ok(user).build();

        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception ex) {
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/getProfilePick/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfilePick(@PathParam("id") Long id) {
        try {
            // Busca o usuário no banco de dados
            User user = em.find(User.class, id);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                             .entity("User not found")
                             .build();
            }

            // Obtém o número da imagem de perfil
            Integer profilePick = user.getProfilePick();

            // Se não tiver imagem definida, retorna um valor padrão (por exemplo, 0)
            if (profilePick == null) {
                profilePick = 0; // Valor padrão
            }

            // Retorna apenas o número da imagem
            return Response.ok(profilePick).build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName())
                 .log(Level.SEVERE, "Error getting profile picture number", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error getting profile picture number: " + e.getMessage())
                         .build();
        }
    }
}
