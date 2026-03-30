/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.controller.ContactController;
import aes.controller.UserController;
import aes.model.AuthenticationToken;
import aes.model.User;
import aes.persistence.AgendaAppointmentDAO;
import aes.persistence.AuthenticationTokenDAO;
import aes.persistence.ChatDAO;
import aes.persistence.ContactDAO;
import aes.persistence.UserDAO;
import aes.utility.EmailHelper;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    private ChatDAO chatDao;
    private AuthenticationTokenDAO authenticationDao;
    private EmailHelper emailHelper;
    
    @Inject
    private ContactController contactController;
    
    @Inject
    private UserController userController;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public UserFacadeREST(){
        super(User.class);
        emailHelper = new EmailHelper();
        try {
            userDAO = new UserDAO();
            contactDAO = new ContactDAO();
            appointmentDao = new AgendaAppointmentDAO();
            chatDao = new ChatDAO();
            authenticationDao = new AuthenticationTokenDAO();
        } catch (NamingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static class EmailClass {
        public String email;
        public EmailClass(String email){
            this.email = email;
        }
    }
    
    public static class UserRegistrationDTO {
        public User user;
        public String password;
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
    @Path("register")
    public Response createUser(UserRegistrationDTO registrationData) {
        try {
            if (registrationData == null || registrationData.user == null || registrationData.password == null || registrationData.password.isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_USER_CREATE reason=INVALID_DATA");

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            User entity = registrationData.user;
            String encryptedPassword = registrationData.password;

            List<User> userList = userDAO.findByEmail(entity.getEmail(), em);

            if (!userList.isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_USER_CREATE reason=EMAIL_IS_ALREADY_IN_USE"
                    + "entityEmail={0}", entity.getEmail());

                return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL_IS_ALREADY_IN_USE").build();
            }

            String decriptedPassword = Encrypter.decrypt(encryptedPassword);

            userDAO.createUser(entity, decriptedPassword, em);

            Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.SEVERE,
                    "User created successfully email={0}",
                    entity.getEmail());

            emailHelper.sendSignUpEmail(entity, em);
            if (entity.isReceiveEmails()) {
                contactDAO.scheduleTipsEmail(entity, em);
                contactDAO.scheduleDiaryReminderEmail(entity, new Date(), em);
                contactDAO.scheduleWeeklyEmail(entity, new Date(), em);
            }

            return Response.status(Response.Status.CREATED).build();
        } catch (EncrypterException | SQLException | MissingResourceException | MessagingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @Path("/createAnonymousUser")
    public Response createAnonymousUser(User entity) throws SQLException {
        try {
            Response r = securityHelper.requireAnonymousToken();
            if (r != null) return r;
            
            if (entity == null || entity.getUnauthenticatedId() == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_ANOYNUMOUS_USER_CREATE reason=INVALID_DATA");

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            String token = securityHelper.getToken();
            AuthenticationToken authToken = authenticationDao.findByToken(token, em);
            
            if (authToken == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_ANONYMOUS_USER_CREATE reason=INVALID_DATA token={0}", token);

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            if (authToken.getUser() != null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] DENIED_ANONYMOUS_USER_CREATE reason=INVALID_USER_OBJECT_RELATION "
                   + "token={0} userId={1}",
                     new Object[]{token, authToken.getUser().getId()});
                
                return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_OBJECT_RELATION").build();
            }
            
            User existingUser = existingUser = userDAO.findByUnauthenticatedId(entity.getUnauthenticatedId(), em);
            
            if (existingUser != null) {
                authToken.setUser(existingUser);
                authenticationDao.update(authToken, em);
                
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.SEVERE,
                        "[SECURITY] ANONYMOUS_USER_REATTACHED unauthenticatedId={0} userId={1}",
                        new Object[]{entity.getUnauthenticatedId(), existingUser.getId()});
                
                return Response.ok(existingUser).build();
            }
            
            userDAO.createAnonymousUser(entity, em);
            
            authToken.setUser(entity);
            authenticationDao.update(authToken, em);
            
            Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.SEVERE,
                    "[SECURITY] ANONYMOUS_USER_CREATED userId={0} unauthenticatedId={1}",
                    new Object[]{entity.getId(), entity.getUnauthenticatedId()});
            
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
            
            return Response.status(Response.Status.CREATED).entity(dto).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    @Path("/recreateAnonymousUser")
    public Response recreateAnonymousUser(User entity) throws SQLException {
        try {
            Response r = securityHelper.requireAnonymousToken();
            if (r != null) return r;
            
            if (entity == null || entity.getUnauthenticatedId() == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_ANONYMOUS_USER_RECREATE reason=INVALID_DATA");

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            String token = securityHelper.getToken();
            AuthenticationToken authToken = authenticationDao.findByToken(token, em);
            
            if (authToken == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_ANONYMOUS_USER_RECREATE reason=INVALID_DATA token={0}", token);

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            // RECREATION OF THE ANONYMOUS USER
            userDAO.createAnonymousUser(entity, em);
            
            // Overwrites the token association
            authToken.setUser(entity);
            authenticationDao.update(authToken, em);
            
            Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.SEVERE,
                    "[SECURITY] ANONYMOUS_USER_RECREATED userId={0} unauthenticatedId={1} token={2}",
                    new Object[]{entity.getId(), entity.getUnauthenticatedId(), token});

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
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("/setInRanking")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setInRanking(User entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            if (entity == null || entity.getInRanking() == null || entity.getNickname() == null || entity.getNickname().isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_SET_USER_IN_RANKING reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }

            userDAO.setInRanking(loggedUser.getId(), entity.getInRanking(), entity.getNickname(), em);

            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("/sendTCLE")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendTCLE() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            userDAO.setSendTCLE(loggedUser.getId(), em);
            
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("recover-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recoverPassword(String userEmail) {
        try {
            Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.WARNING, "[INFO] FORGET_PASSWORD" + "actorUserEmail = {0}", userEmail);
            
            User u = userDAO.generateRecoverCode(userEmail, em);
            emailHelper.sendPasswordRecoveryEmail(u, em);
            
            Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.SEVERE, "[INFO] RECOVER_PASSWORD_SERVICE" + "actorUserId = {0}", u.getId());

            return Response.status(Response.Status.OK).build();
        } catch (SQLException | MessagingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("deleteAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Secured
    public Response deleteAccount() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            String token = securityHelper.getToken();
            
            emailHelper.sendDeleteAccountEmail(loggedUser, token, em);
            
            Logger.getLogger(UserFacadeREST.class.getName())
                .log(Level.SEVERE, "[INFO] DELETE_ACCOUNT_SERVICE" + "actorUserId = {0}", loggedUser.getId());
            
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | MessagingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @GET
    @Secured
    @Path("login/{token}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response login(@PathParam("token") String tkn) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            String token = securityHelper.getToken();

            if (!tkn.equals(token)) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.SEVERE, "[SECURITY] DENIED_LOGIN reason=PARAM_TOKEN_DIFFER_FROM_HEADER_TOKEN "
                    + "param_token={0} header_token={1}", new Object[]{tkn, token});
                
                return Response.status(Response.Status.UNAUTHORIZED).entity("ACCESS_DENIED").build();
            }
            
            User user = userDAO.login(tkn, em);
            
            return Response.status(Response.Status.OK).entity(user).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return null;
        }
    }
    
    /*
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("findUserByNickname/{nickname}")
    public Response findUserByNickname(@PathParam("nickname") String nickname) {
        RESTApiResponse response;
        try {
            User userResult = (User) em.createQuery("SELECT u FROM User u WHERE u.nickname=:nm")
                .setParameter("nm", nickname)
                .getSingleResult();
        
            if (userResult == null) {
                response = new RESTApiResponse("Usuário não encontrado");
                return Response.status(Response.Status.CONFLICT).entity(response.getMessage()).build();
            } else {
                response = new RESTApiResponse(userResult);
                return Response.status(Response.Status.OK).entity(response.getEntityData()).build();
            }    
        } catch (Exception ex) {
            response = new RESTApiResponse("Ocorreu um erro: " + ex);
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.getMessage()).build();
        }
    }
    */
    
    @PUT
    @Path("/changeTitle")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeTitle(User entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null || entity.getSelected_title() == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_CHANGE_TITLE reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            userDAO.setTitle(loggedUser.getId(), entity.getSelected_title(), em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    /*
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
        RESTApiResponse response;
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
                response = new RESTApiResponse("O usuário já é consultor ou o consultor indicado não é consultor.");
                return Response.status(Response.Status.BAD_REQUEST).entity(response.getMessage()).build();
            }
            response = new RESTApiResponse("E-mail e/ou senha incorretos ou sem permissão de administrador.");
            return Response.status(Response.Status.FORBIDDEN).entity(response.getMessage()).build();
        } catch (SQLException | RuntimeException | EncrypterException ex) {
            response = new RESTApiResponse("Ocorreu um erro: " + ex);
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.getMessage()).build();
        }
    }
    */
    
    @PUT
    @Path("/updateEvaluationProfile")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvaluationProfile(User entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null || entity.getEducation() == null || entity.getEmployed() == null || entity.getKnowWebsite() == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_UPDATE_EVALUATION_PROFILE reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            userDAO.updateEvaluationProfile(loggedUser.getId(), entity, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findUserByChatId/{chatId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response findUserByChatId(@PathParam("chatId") Long chatId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;
            
            User user = chatDao.findUserById(chatId, em);
            return Response.ok().entity(user.getId()).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("validate-referral-code")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateReferralCode(String referralCode) {       
        try {
            if (referralCode == null || referralCode.isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_VALIDATE_REFERRAL_CODE reason=INVALID_DATA");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            User referrer = userDAO.findByReferralCode(referralCode, em);

            if (referrer == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_VALIDATE_REFERRAL_CODE reason=TARGET_USER_NOT_FOUND");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("TARGET_USER_NOT_FOUND").build();
            }

            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("set-friend-referral-code")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response setFriendReferralCode(String referralCode) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (referralCode == null || referralCode.isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_SET_REFERRAL_CODE reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            if (loggedUser.getMyReferralCode() != null && !loggedUser.getMyReferralCode().isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_SET_REFERRAL_CODE reason=INVALID_USER_OBJECT_RELATION "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_USER_OBJECT_RELATION").build();
            }
            
            userDAO.updateReferralCode(loggedUser.getId(), referralCode, em);
            
            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("count-referral-usage")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response countReferralCodeUsage(String referralCode) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (referralCode == null || referralCode.isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_SET_REFERRAL_CODE reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            long count = userDAO.countReferralCodeUsage(referralCode, em);
            
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("get-referral-code")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserReferralCode() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            String referralCode = loggedUser.getMyReferralCode();
            
            if (referralCode == null || referralCode.isEmpty()) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_FOUND_REFERRAL_CODE reason=INVALID_USER_OBJECT_RELATION "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("INVALID_USER_OBJECT_RELATION").build();
            }
            
            return Response.status(Response.Status.OK).entity(referralCode).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("updateAppSignInDate")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAppSignInDate() throws ParseException {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            loggedUser.setAppSignInDate(new Date());
            userDAO.update(loggedUser, em);
            
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("updateAdmin/{isAdmin}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAdmin(@PathParam("isAdmin") boolean isAdmin, @PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireAdmin(loggedUser);
            if (r != null) return r;
            
            User user = userDAO.find(userId, em);
            
            if (user == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_FOUND_USER reason=INVALID_DATA "
                    + "loggedUserId={0}, targetUserId={1}", new Object[]{loggedUser.getId(), userId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("INVALID_DATA").build();
            }
            
            user.setAdmin(isAdmin);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("updateConsultant/{isConsultant}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConsultant(@PathParam("isConsultant") boolean isConsultant, @PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireAdmin(loggedUser);
            if (r != null) return r;
            
            User user = userDAO.find(userId, em);
            
            if (user == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_FOUND_USER reason=INVALID_DATA "
                    + "loggedUserId={0}, targetUserId={1}", new Object[]{loggedUser.getId(), userId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("INVALID_DATA").build();
            }
            
            user.setConsultant(isConsultant);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("updateUseChatbot/{useChatbot}/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUseChatbot(@PathParam("useChatbot") boolean useChatbot, @PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireAdmin(loggedUser);
            if (r != null) return r;
            
            User user = userDAO.find(userId, em);
            
            if (user == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_FOUND_USER reason=INVALID_DATA "
                    + "loggedUserId={0}, targetUserId={1}", new Object[]{loggedUser.getId(), userId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("INVALID_DATA").build();
            }
            
            user.setUse_chatbot(useChatbot);
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    /*
    @PUT
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(User user) {
        RESTApiResponse response;
        try {
            userDAO.update(user, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            response = new RESTApiResponse("Ocorreu um erro: " + ex);
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.getMessage()).build();
        }
    }
    */

    
    @GET
    @Path("listForAdmin")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllUsers() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireAdmin(loggedUser);
            if (r != null) return r;
            
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
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @GET
    @Path("info/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getUserInfo(@PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (userId == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_FOUND_USER_INFO reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            User user = em.find(User.class, userId);

            if (user == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] USER_DOES_NOT_EXISTS reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("INVALID_DATA").build();
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getNickname());
            userInfo.put("birthDate", user.getBirthDate());
            userInfo.put("gender", user.getGender());

            return Response.ok(userInfo).build();

        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findUsersByConsultor")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getUsersByConsultor() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;
            
            List<User> clients = userDAO.findUsersByConsultantId(loggedUser.getId(), em);

            List<Map<String, Object>> response = clients.stream()
                .map(client -> {
                    Map<String, Object> clientInfo = new HashMap<>();
                    clientInfo.put("_id", client.getId());
                    clientInfo.put("name", client.getName());
                    clientInfo.put("email", client.getEmail());
                    clientInfo.put("nickname", client.getNickname());
                    clientInfo.put("registration_complete", client.isRegistration_complete());
                    return clientInfo;
                })
                .collect(Collectors.toList());

            return Response.ok(response).build();

        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findRelatedConsultantByUserId")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getRelatedConsultantByUser() {
        try {
            Response r = securityHelper.requireAuthenticatedAnonymous();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (loggedUser.getRelatedConsultant() == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] USER_DOES_NOT_HAVE_RELATED_CONSULTANT reason=TARGET_USER_NOT_FOUND "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }
            
            return Response.ok(loggedUser.getRelatedConsultant().getId()).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("updateProfilePick/{profilePick}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfilePick(@PathParam("profilePick") Integer profilePick) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            if (profilePick == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] INVALID_PROFILE_PICK reason=INVALID_DATA "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            loggedUser.setProfilePick(profilePick);
            
            userDAO.update(loggedUser, em);

            return Response.ok().build();
        } catch (SQLException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("getMyProfilePick")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getProfilePick() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            Integer profilePick = loggedUser.getProfilePick();

            if (profilePick == null) profilePick = 0; // fallback

            return Response.ok(profilePick).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("getProfilePick/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getProfilePick(@PathParam("id") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            User user = em.find(User.class, userId);

            if (user == null) {
                Logger.getLogger(UserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DID_NOT_FIND_USER reason=TARGET_USER_NOT_FOUND "
                    + "loggedUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }

            Integer profilePick = user.getProfilePick();

            if (profilePick == null) profilePick = 0; // fallback
            
            return Response.ok(profilePick).build();
        } catch (Exception ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @PUT
    @Path("/updateTutorialSeen/{id}/{status}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTutorialSeen(@PathParam("id") Long id, @PathParam("status") Boolean status) {
        try {
            User user = em.find(User.class, id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // Define se viu (true) ou não (false)
            user.setTutorialSeen(status);

            // Usando o padrão do projeto para salvar no banco (sem precisar do userTransaction)
            userDAO.update(user, em);

            // Retorna json confirmação
            return Response.ok("{\"tutorial_seen\": " + status + "}").build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao atualizar status do tutorial", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/getTutorialSeen/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTutorialSeen(@PathParam("id") Long id) {
        try {
            User user = em.find(User.class, id);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                             .entity("User not found")
                             .build();
            }

            // Pega o status (o getter já trata nulos como false)
            Boolean seen = user.getTutorialSeen();

            // Retorna um JSON simples
            return Response.ok("{\"tutorial_seen\": " + seen + "}").build();

        } catch (Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName())
                  .log(Level.SEVERE, "Erro ao buscar status do tutorial", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Erro ao processar requisição: " + e.getMessage())
                          .build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
