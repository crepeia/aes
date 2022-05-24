/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.EvaluationDAO;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("secured/evaluation")
@TransactionManagement(TransactionManagementType.BEAN)
public class EvaluationFacadeREST extends AbstractFacade<Evaluation> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private EvaluationDAO evaluationDAO;
    
    @Context
    SecurityContext securityContext;

    public EvaluationFacadeREST() {
        super(Evaluation.class);
        try {
            evaluationDAO = new EvaluationDAO();
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Evaluation create(Evaluation entity) {
        try {
            //super.create(entity);
            evaluationDAO.create(entity, em);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @GET
    @Path("find/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("userId") Long userId) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();
            
           /* List<Evaluation> evList = getEntityManager().createQuery("SELECT e FROM Evaluation e WHERE e.user.id=:userId AND e.user.email=:userEmail")
                    .setParameter("userId", userId)
                    .setParameter("userEmail", userEmail)
                    .getResultList();
            
            if(evList.size() > 0){
                return Response.ok().entity(evList.get(evList.size()-1)).build();
            } else {
                //System.out.println("service.EvaluationFacadeREST.find() create");
                Evaluation ev = new Evaluation();
                ev.setDateCreated(new Date());
                ev.setUser(em.find(User.class, userId));
                super.create(ev);}*/
                Evaluation ev = evaluationDAO.find(userId, userEmail, em);
                return Response.ok().entity(ev).build();
            

        } catch (SQLException e) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        
        return String.valueOf(evaluationDAO.count(em));
        //return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
