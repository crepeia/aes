/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.DailyLog;
import aes.persistence.DailyLogDAO;
import aes.utility.Secured;
import java.sql.SQLException;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@Path("secured/dailylog")
@TransactionManagement(TransactionManagementType.BEAN)
public class DailyLogFacadeREST extends AbstractFacade<DailyLog> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private DailyLogDAO dailyLogDAO;

    public DailyLogFacadeREST() {
        super(DailyLog.class);
        try {
            dailyLogDAO = new DailyLogDAO();
        } catch (NamingException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Path("editOrCreate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editOrCreate(DailyLog entity) {
        try {
            String action = "";
            boolean edited = dailyLogDAO.edit(entity, em);

            if (edited) {
                action = "edit";
            } else {
                dailyLogDAO.insert(entity, em);
                action = "create";
            }

            return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).build();
        } catch (JSONException | SQLException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();

        }
        /*try {
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            //Date t = format.parse(entity.getLogDate().toString());
            
            DailyLog dl = (DailyLog) getEntityManager().createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate")
            .setParameter("recordId", entity.getRecord().getId())
            .setParameter("logDate", entity.getLogDate())
            .getSingleResult();
            dl.setDrinks(entity.getDrinks());
            dl.setContext(entity.getContext());
            dl.setConsequences(entity.getConsequences());
            super.edit(dl);
            action = "edit";
            
            } catch( NoResultException e ) {
            super.create(entity);
            action = "create";
            //return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).build();
            } catch (Exception e) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
            
            try {
            return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).type(MediaType.APPLICATION_JSON).build();
            } catch (JSONException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
            }*/

    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        try {
            //super.remove(super.find(id));
            dailyLogDAO.delete(dailyLogDAO.find(id, em), em);
        } catch (SQLException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("find/{recordId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<DailyLog> find(@PathParam("recordId") Long recordId) {
       /* return getEntityManager().createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId")
                .setParameter("recordId", recordId)
                .getResultList();*/
       return dailyLogDAO.find(recordId, em);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
