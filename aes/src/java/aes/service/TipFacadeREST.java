/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Tip;
import aes.persistence.TipDAO;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.List;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
/**
 *
 * @author bruno
 */
@Stateless
@Secured
@TransactionManagement(TransactionManagementType.BEAN)

@Path("tip")
public class TipFacadeREST extends AbstractFacade<Tip> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private TipDAO tipDAO;

    public TipFacadeREST() {
        super(Tip.class);
     
        try {
            tipDAO = new TipDAO();
        } catch (NamingException ex) {
            Logger.getLogger(TipFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Tip find(@PathParam("id") Long id) {
        return tipDAO.find(id, em);
    }

    @GET
    @Path("all")
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Tip> findAll() {
        try {
            return tipDAO.list(em);
        } catch (SQLException ex) {
            Logger.getLogger(TipFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
