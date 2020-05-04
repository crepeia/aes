/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import aes.model.DailyLog;
import aes.model.Record;
import aes.utility.Secured;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
public class DailyLogFacadeREST extends AbstractFacade<DailyLog> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public DailyLogFacadeREST() {
        super(DailyLog.class);
    }
/*
    @POST
    @Override
    @Path("create/{recordId}/{logDate}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(@PathParam("recordId") Long recordId, @PathParam("logDate") String ld, DailyLog entity) {
        super.create(entity);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date logDate = sdf.parse(ld);
            
            //Record r = em.find(Record.class, recordId);
            //entity.setRecord(r);
            
        } catch (ParseException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

    
    @POST
    @Path("create")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Override
    public void create(DailyLog entity) {
        try {
        DailyLog dl = (DailyLog) getEntityManager().createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate")
                .setParameter("recordId", entity.getRecord().getId())
                .setParameter("logDate", entity.getLogDate())
                .getSingleResult();
        } catch( NoResultException e ) {
            super.create(entity);
        }
    }
    */
    
    @POST
    @Path("editOrCreate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editOrCreate(DailyLog entity) {
        String action = "";
        try {
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
            return Response.serverError().build();
        }
        
        try {
            return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (JSONException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }
     
   
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("find/{recordId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<DailyLog> find(@PathParam("recordId") Long recordId) {
        return getEntityManager().createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId")
                .setParameter("recordId", recordId)
                .getResultList();
    }


    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
