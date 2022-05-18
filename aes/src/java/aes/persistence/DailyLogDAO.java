/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.DailyLog;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author patri
 */
public class DailyLogDAO extends GenericDAO<DailyLog>{
    
    public DailyLogDAO() throws NamingException {
        super(DailyLog.class);
    }
    

    public DailyLog editOrCreate(DailyLog entity, EntityManager entityManager) throws SQLException {
        //String action = "";
        try {
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            //Date t = format.parse(entity.getLogDate().toString());
            
            DailyLog dl = (DailyLog) entityManager.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate")
                .setParameter("recordId", entity.getRecord().getId())
                .setParameter("logDate", entity.getLogDate())
                .getSingleResult();
                dl.setDrinks(entity.getDrinks());
                dl.setContext(entity.getContext());
                dl.setConsequences(entity.getConsequences());
                super.insertOrUpdate(dl, entityManager);
                //action = "edit";
                return dl;
            
        } catch( NoResultException e ) {
             super.insertOrUpdate(entity, entityManager);
             return entity;
            //action = "create";
            //return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).build();
           
        } //catch (Exception e) {
           // Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, e);
        //    return null;
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
       // }
        
//        try {
//            return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).type(MediaType.APPLICATION_JSON).build();
//        } catch (JSONException ex) {
//            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
//        }
        
 
    }
     

    public void remove(Long id, EntityManager entityManager) throws SQLException {
        super.delete(super.find(id, entityManager), entityManager);
        //super.remove(super.find(id));
    }


    public List<DailyLog> find(Long recordId, EntityManager entityManager) {
        return entityManager.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId")
                .setParameter("recordId", recordId)
                .getResultList();
        
    }
    
}
