/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Question;
import aes.model.QuestionUser;
import aes.model.User;
import aes.persistence.QuestionDAO;
import aes.persistence.QuestionUserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.text.ParseException;
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
@Path("questionuser")
@Secured
@TransactionManagement(TransactionManagementType.BEAN)
public class QuestionUserFacadeREST extends AbstractFacade<QuestionUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private QuestionDAO questionDao;
    private QuestionUserDAO questionUserDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public QuestionUserFacadeREST() {
        super(QuestionUser.class);
        try {
            questionDao = new QuestionDAO();
            questionUserDao = new QuestionUserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }  

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("findAll/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllQuestions() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            List<Question> list = questionDao.findAllQuestion(em);
         
            return Response.ok().entity(list).build();
        } catch (Exception ex) {
            Logger.getLogger(QuestionUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    } 
    
    @GET
    @Path("findByUser")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findUserQuestions() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<QuestionUser> list = questionDao.findUserQuestions(loggedUser.getId(), em);
            
            return Response.ok().entity(list).build();
        } catch (Exception ex) {
            Logger.getLogger(QuestionUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findLastUserAnswer/{currentDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findLastUserAnswer(@PathParam("currentDate") String cD) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            if (cD.isEmpty()) {
                Logger.getLogger(QuestionUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_FIND_LAST_USER_ANSWER reason=INVALID_DATA");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<QuestionUser> list = questionDao.findLastUserAnswer(cD, loggedUser.getId(), em);
                      
            return Response.ok().entity(list).build();
        } catch (ParseException ex) {
            Logger.getLogger(QuestionUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createQuestionUser(QuestionUser entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            if (entity == null || entity.getQuestion() == null || entity.getAnswer() == null) {
                Logger.getLogger(QuestionUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_QUESTION_INSERT reason=INVALID_DATA");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            Question question = questionDao.findById(entity.getQuestion().getId(), em);
            
            if (question == null) {
                Logger.getLogger(QuestionUserFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURITY] DENIED_QUESTION_INSERT reason=INVALID_DATA");
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            User loggedUser = securityHelper.getLoggedUser();
            
            QuestionUser newEntity = new QuestionUser();
            newEntity.setUser(loggedUser);
            newEntity.setQuestion(question);
            newEntity.setAnswer(entity.getAnswer());
            newEntity.setDateCreated(LocalDate.now());
            
            questionUserDao.insert(newEntity, em);
            return Response.status(Response.Status.CREATED).build();            
        } catch (SQLException ex) {
            Logger.getLogger(QuestionUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
}
