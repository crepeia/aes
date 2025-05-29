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
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
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
    
//    @POST
//    @Path("/validate-referral-code")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response validateReferralCode(String jsonInput) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode node = mapper.readTree(jsonInput);
//            String referralCode = node.get("referral_code").asText();
//
//            // SOLUÇÃO DEFINITIVA - Native Query com o nome exato da coluna
//            Query query = em.createNativeQuery(
//                "SELECT * FROM tb_user WHERE `my_referral_code` = ?", 
//                User.class);
//            query.setParameter(1, referralCode);
//
//            List<User> referrers = query.getResultList();
//
//            if (referrers.isEmpty()) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"valid\":false, \"message\":\"Código inválido ou não encontrado\"}")
//                    .build();
//            }
//
//            return Response.ok()
//                .entity("{\"valid\":true, \"message\":\"Código válido\", \"referrerId\":" + 
//                       referrers.get(0).getId() + "}")
//                .build();
//
//        } catch (Exception e) {
//            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                .entity("{\"valid\":false, \"message\":\"Erro: " + e.getMessage() + "\"}")
//                .build();
//        }
//    }
    
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
    
//    @POST
//    @Path("/set-friend-referral-code")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response setFriendReferralCode(String jsonInput) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode node = mapper.readTree(jsonInput);
//
//            // Verificação dos campos obrigatórios
//            if (!node.has("id") || !node.has("code")) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"success\":false, \"message\":\"Campos 'id' e 'code' são obrigatórios\"}")
//                    .build();
//            }
//
//            Long userId = node.get("id").asLong();
//            String referralCode = node.get("code").asText();
//
//            // Validação dos dados
//            if (userId == null || referralCode == null || referralCode.trim().isEmpty()) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"success\":false, \"message\":\"ID inválido ou código vazio\"}")
//                    .build();
//            }
//
//            User user = em.find(User.class, userId);
//
//            if (user == null) {
//                return Response.status(Response.Status.NOT_FOUND)
//                    .entity("{\"success\":false, \"message\":\"Usuário não encontrado\"}")
//                    .build();
//            }
//
//            // Verifica se o usuário já possui um código
//            if (user.getMyReferralCode() != null && !user.getMyReferralCode().isEmpty()) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"success\":false, \"message\":\"Usuário já possui um código de referência\"}")
//                    .build();
//            }
//
//            // Transação explícita
//            userTransaction.begin();
//            user.setMyReferralCode(referralCode);
//            em.merge(user);
//            userTransaction.commit();
//
//            return Response.ok()
//                .entity("{\"success\":true, \"message\":\"Código de referência atualizado com sucesso\"}")
//                .build();
//
//        } catch (JsonProcessingException e) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                .entity("{\"success\":false, \"message\":\"JSON inválido\"}")
//                .build();
//        } catch (Exception e) {
//            try {
//                if (userTransaction != null) userTransaction.rollback();
//            } catch (Exception ex) {
//                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao fazer rollback", ex);
//            }
//
//            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido - verifique logs";
//            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);
//
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                .entity("{\"success\":false, \"message\":\"" + errorMsg + "\"}")
//                .build();
//        }
//    }
    
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
    
//    @POST
//    @Path("/count-referral-usage")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response countReferralCodeUsage(String jsonInput) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode node = mapper.readTree(jsonInput);
//
//            // Verifica se o campo referral_code está presente
//            if (!node.has("referral_code")) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"error\":\"O campo 'referral_code' é obrigatório\"}")
//                    .build();
//            }
//
//            String referralCode = node.get("referral_code").asText();
//
//            // Consulta para contar quantas vezes o código aparece
//            Query query = em.createNativeQuery(
//                "SELECT COUNT(*) FROM tb_user WHERE friend_referral_code = ?");
//            query.setParameter(1, referralCode);
//
//            // O resultado é um BigInteger que convertemos para long
//            long count = ((Number)query.getSingleResult()).longValue();
//
//            return Response.ok()
//                .entity("{\"count\":" + count + ", \"referral_code\":\"" + referralCode + "\"}")
//                .build();
//
//        } catch (JsonProcessingException e) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                .entity("{\"error\":\"JSON inválido\"}")
//                .build();
//        } catch (Exception e) {
//            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "ERRO DETALHADO: ", e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                .entity("{\"error\":\"Erro ao processar a requisição\"}")
//                .build();
//        }
//    }
    
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
}
