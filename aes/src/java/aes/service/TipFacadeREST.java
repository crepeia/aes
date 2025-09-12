/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Tip;
import aes.persistence.TipDAO;
import aes.utility.RESTApiResponse;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
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
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        try {
            return Response.ok().entity(tipDAO.listOnce("id", id, em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("findAll")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllTips() {
        try {
            return Response.ok().entity(tipDAO.list(em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response update(Tip tip) {
        try {
            boolean isDescriptionsNull = Objects.isNull(tip.getDescriptionPT()) || Objects.isNull(tip.getDescriptionEN()) || Objects.isNull(tip.getDescriptionES());
            boolean isDescriptionsEmpty = Objects.equals(tip.getDescriptionPT(), "") || Objects.equals(tip.getDescriptionEN(), "") || Objects.equals(tip.getDescriptionES(), "");
            if(isDescriptionsNull || isDescriptionsEmpty) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            tipDAO.update(tip, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NotificationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
