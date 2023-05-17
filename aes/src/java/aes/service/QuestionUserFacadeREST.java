/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.MedalUser;
import aes.model.Question;
import aes.model.QuestionUser;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
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
public class QuestionUserFacadeREST extends AbstractFacade<QuestionUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public QuestionUserFacadeREST() {
        super(QuestionUser.class);
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
            List<Question> list = (List<Question>) em.createQuery("SELECT q FROM Question q")
            .getResultList();
         
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    } 
    
    @GET
    @Path("find/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findUserQuestions(@PathParam("userId") String uId) {
        try {
            List<QuestionUser> list = (List<QuestionUser>) em.createQuery("SELECT qu FROM QuestionUser qu WHERE qu.user.id=:userId")
                .setParameter("userId", Long.parseLong(uId))
                .getResultList();
            
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("findLastUserAnswer/{currentDate}/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findLastUserAnswer(@PathParam("currentDate") String cD,@PathParam("userId") String userId) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = sdf.parse(cD);
            List<QuestionUser> list = (List<QuestionUser>) em.createQuery("SELECT qu FROM QuestionUser qu WHERE qu.user.id=:userId AND qu.dateCreated=:currentDate")
                .setParameter("userId", Long.parseLong(userId))
                .setParameter("currentDate", currentDate.toInstant().atZone( ZoneId.systemDefault() ).toLocalDate())
                .getResultList();            
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createQuestionUser(QuestionUser entity) {
        try {
            QuestionUser newEntity = super.create(entity);
            return true;
            
        } catch (Exception e) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
}
