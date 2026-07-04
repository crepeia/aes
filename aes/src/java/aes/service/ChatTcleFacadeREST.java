package aes.service;

import aes.model.ChatTCLE;
import aes.model.User;
import aes.persistence.ChatTcleDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author luansb
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("secured/chat-tcle")
@Secured
public class ChatTcleFacadeREST extends AbstractFacade<ChatTCLE> {
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private UserDAO userDAO;
    private ChatTcleDAO chatTcleDAO;
    
    @Inject
    private SecurityContextHelper securityHelper;
    
    public ChatTcleFacadeREST(){
        super(ChatTCLE.class);
        try {
            userDAO = new UserDAO();
            chatTcleDAO = new ChatTcleDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ChatTcleFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public static class ChatTcleRequest {
        private Boolean agreed;
        private String tcleVersion;

        public Boolean getAgreed() {
            return agreed;
        }

        public void setAgreed(Boolean agreed) {
            this.agreed = agreed;
        }

        public String getTcleVersion() {
            return tcleVersion;
        }

        public void setTcleVersion(String tcleVersion) {
            this.tcleVersion = tcleVersion;
        }
    };
    
    public static class ChatTcleResponse {
        private Boolean agreed;
        private String tcleVersion;
        private Date decisionDate;
        
        public Boolean getAgreed() {
            return agreed;
        }

        public void setAgreed(Boolean agreed) {
            this.agreed = agreed;
        }

        public String getTcleVersion() {
            return tcleVersion;
        }

        public void setTcleVersion(String tcleVersion) {
            this.tcleVersion = tcleVersion;
        }
        
        public Date getDecisionDate() {
            return decisionDate;
        }
        
        public void setDecisionDate(Date decisionDate) {
            this.decisionDate = decisionDate;
        }
    };
    
    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(ChatTcleRequest request) {
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (request.getAgreed() == null || request.getTcleVersion() == null || request.getTcleVersion().trim().isEmpty()) {
                Logger.getLogger(ChatTcleFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_CHAT_TCLE_INSERTION reason=INVALID_DATA "
                        + "actorUserId={0}",
                        new Object[]{loggedUser.getId()});
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            ChatTCLE latest = chatTcleDAO.getLatestByUser(loggedUser, em);
            if (latest != null
                    && latest.getAgreed().equals(request.getAgreed())
                    && latest.getTcleVersion().equals(request.getTcleVersion())) {

                return Response.status(Response.Status.NO_CONTENT).build();
            }
            
            ChatTCLE chatTcle = new ChatTCLE();
            chatTcle.setUser(loggedUser);
            chatTcle.setAgreed(request.getAgreed());
            chatTcle.setTcleVersion(request.getTcleVersion());
            chatTcle.setDecisionDate(new Date());
            
            System.out.println("userId=" + loggedUser.getId());
            System.out.println("agreed=" + request.getAgreed());
            System.out.println("tcleVersion=" + request.getTcleVersion());
            System.out.println("decisionDate=" + new Date());
            
            chatTcleDAO.insert(chatTcle, em);
            
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException ex) {
            Logger.getLogger(ChatTcleFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Path("latest")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLatestDecision() {
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            ChatTCLE latest = chatTcleDAO.getLatestByUser(loggedUser, em);

            if (latest == null) {
                Logger.getLogger(ChatTcleFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_CHAT_TCLE_GET reason=TARGET_OBJECT_NOT_FOUND "
                        + "actorUserId={0}",
                        new Object[]{loggedUser.getId()});
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            ChatTcleResponse response = new ChatTcleResponse();
            response.setAgreed(latest.getAgreed());
            response.setTcleVersion(latest.getTcleVersion());
            response.setDecisionDate(latest.getDecisionDate());

            return Response.ok(response).build();
        } catch (Exception ex) {
            Logger.getLogger(ChatTcleFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }      
    }
}
