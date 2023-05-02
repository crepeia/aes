/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Question;
import aes.persistence.QuestionDAO;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Matheus Carvalho
 */
@Stateless
@Path("question")
public class QuestionFacadeREST extends AbstractFacade<Question> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private QuestionDAO questionDAO;

    public QuestionFacadeREST() {
        super(Question.class);
        
        try {
            questionDAO = new QuestionDAO();
        } catch (NamingException ex) {
            Logger.getLogger(QuestionFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Question find(@PathParam("id") Long id) {
        return questionDAO.find(id, em);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
