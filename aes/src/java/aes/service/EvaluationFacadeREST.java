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
    
    @Context
    SecurityContext securityContext;

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
            
            if (ev == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
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
    
    @POST
    @Path("createEvaluation/{userId}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createEvaluation(@PathParam("userId") Long userId, Evaluation newEvaluation) {
        //Teste
//    public Response createEvaluation(@PathParam("userId") Long userId) {
//            Evaluation newEvaluation = new Evaluation();
        if(newEvaluation == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Evaluation object cannot be null").build();
        }
        
        if(userId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User ID must be provided").build();
        }
        
        // Tratando o caso do usuário com ID passado não existir na tabela.
        User user = userDAO.find(userId, em);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User does not exist").build();
        }
        
        newEvaluation.setUser(user);
        
        try {
            evaluationDAO.createEvaluation(newEvaluation, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException e) {
            Logger.getLogger(EvaluationFacadeREST.class.getName()).log(Level.SEVERE, "Error creating Evaluation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error inserting Evaluation: " + e.getMessage()).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("dates/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluationDatesByUser(@PathParam("userId") Long userId) {
        try {
            // Verifica se o usuário existe
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Usuário não encontrado\"}")
                    .build();
            }

            // Busca as datas das avaliações do usuário
            List<Date> dates = em.createQuery(
                "SELECT e.dateCreated FROM Evaluation e WHERE e.user.id = :userId", Date.class)
                .setParameter("userId", userId)
                .getResultList();

            return Response.ok(dates).build();

        } catch (Exception e) {
            Logger.getLogger(EvaluationFacadeREST.class.getName())
                .log(Level.SEVERE, "Erro ao buscar datas de avaliações", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao processar a requisição\"}")
                .build();
        }
    }
    
    @GET
    @Path("findByUserAndDate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluationByUserAndDate(
        @QueryParam("userId") Long userId,
        @QueryParam("date") String dateStr) {

        try {
            if (userId == null || dateStr == null || dateStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Parâmetros obrigatórios não informados\"}")
                    .build();
            }

            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Usuário não encontrado\"}")
                    .build();
            }

            // Tenta parsear a data com milissegundos e timezone
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date date;
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Formato de data inválido\"}")
                    .build();
            }

            // Adiciona um intervalo para considerar imprecisão de milissegundos
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            cal.add(Calendar.SECOND, -5); // 5 segundos antes
            Date start = cal.getTime();

            cal.add(Calendar.SECOND, 10); // volta ao original + 5 segundos
            Date end = cal.getTime();

            // Apenas para debug, pode remover depois
            System.out.println("Data recebida: " + dateStr);
            System.out.println("Data parseada: " + date);
            System.out.println("Intervalo: " + start + " até " + end);

            // Busca a avaliação dentro do intervalo
            TypedQuery<Evaluation> query = em.createQuery(
                "SELECT e FROM Evaluation e WHERE e.user.id = :userId AND e.dateCreated BETWEEN :start AND :end",
                Evaluation.class);
            query.setParameter("userId", userId);
            query.setParameter("start", start);
            query.setParameter("end", end);

            List<Evaluation> result = query.getResultList();

            if (result.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Avaliação não encontrada\"}")
                    .build();
            }

            return Response.ok(result.get(0)).build();

        } catch (Exception e) {
            Logger.getLogger(EvaluationFacadeREST.class.getName())
                .log(Level.SEVERE, "Erro ao buscar avaliação", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao processar a requisição\"}")
                .build();
        }
    }
}
