/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.EvaluationDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    private UserDAO userDAO;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public EvaluationFacadeREST() {
        super(Evaluation.class);
        try {
            evaluationDAO = new EvaluationDAO();
            userDAO = new UserDAO();
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
    @Path("find")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();

            Evaluation ev = evaluationDAO.find(loggedUser.getId(), em);
            
            if (ev == null) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] NON_EXISTENT_EVALUATION reason=TARGET_OBJECT_NOT_FOUND "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            return Response.ok().entity(ev).build();
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    /*@GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        
        return String.valueOf(evaluationDAO.count(em));
        //return String.valueOf(super.count());
    }*/
    
    @POST
    @Path("createEvaluation")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createEvaluation(Evaluation newEvaluation) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            if(newEvaluation == null) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_EVALUATION reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }

            newEvaluation.setUser(loggedUser);
            newEvaluation.setDateCreated(new Date());
        
            evaluationDAO.createEvaluation(newEvaluation, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, "Error creating Evaluation: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("dates/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluationDatesByUser(@PathParam("userId") Long userId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;
            
            User user = em.find(User.class, userId);
            if (user == null) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] NON_EXISTENT_USER reason=TARGET_USER_NOT_FOUND "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }
            
            // Just info
            boolean valid = securityHelper.isValidUserConsultantRelation(user, loggedUser);
            
            List<Date> dates = evaluationDAO.listDatesByUser(user.getId(), em);

            return Response.ok(dates).build();

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, "Error creating Evaluation: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findByUserAndDate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluationByUserAndDate(@QueryParam("userId") Long userId, @QueryParam("date") String dateStr) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;
            
            User user = em.find(User.class, userId);
            if (user == null) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] NON_EXISTENT_USER reason=TARGET_USER_NOT_FOUND "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }
            
            if (dateStr == null || dateStr.trim().isEmpty()) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_PARAM_OBJECT reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }

            // Tenta parsear a data com milissegundos e timezone
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date date;
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_DATE reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }

            // Adiciona um intervalo para considerar imprecisão de milissegundos
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            cal.add(Calendar.SECOND, -5); // 5 segundos antes
            Date start = cal.getTime();

            cal.add(Calendar.SECOND, 10); // volta ao original + 5 segundos
            Date end = cal.getTime();

            // Busca a avaliação dentro do intervalo
            List<Evaluation> result = evaluationDAO.findByUserAndDate(user.getId(), start, end, em);

            if (result.isEmpty()) {
                Logger.getLogger(EvaluationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] NON_EXISTENT_EVALUATION reason=TARGET_OBJECT_NOT_FOUND "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }

            return Response.ok(result.get(0)).build();

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, "Error creating Evaluation: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
