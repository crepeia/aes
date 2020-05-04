/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import aes.model.Tip;
import aes.utility.Secured;
import java.util.List;
import javax.ejb.Stateless;
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
 * @author bruno
 */
@Stateless
@Secured
@Path("tip")
public class TipFacadeREST extends AbstractFacade<Tip> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public TipFacadeREST() {
        super(Tip.class);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Tip find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Path("all")
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Tip> findAll() {
        return super.findAll();
    }
 
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
