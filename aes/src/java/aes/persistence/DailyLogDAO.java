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
    
    public DailyLogDAO(EntityManager entityManager) throws NamingException {
        super(DailyLog.class);
        this.setEntityManager(entityManager);

    }
    

    public DailyLog editOrCreate(DailyLog entity) throws SQLException {
        //String action = "";
        try {
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            //Date t = format.parse(entity.getLogDate().toString());
            
            DailyLog dl = (DailyLog) getEntityManager().createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate")
                .setParameter("recordId", entity.getRecord().getId())
                .setParameter("logDate", entity.getLogDate())
                .getSingleResult();
                dl.setDrinks(entity.getDrinks());
                dl.setContext(entity.getContext());
                dl.setConsequences(entity.getConsequences());
                super.insertOrUpdate(dl);
                //action = "edit";
                return dl;
            
        } catch( NoResultException e ) {
             super.insertOrUpdate(entity);
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
     

    public void remove(Long id) throws SQLException {
        super.delete(super.find(id));
        //super.remove(super.find(id));
    }


    public List<DailyLog> find(Long recordId) {
        return getEntityManager().createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId")
                .setParameter("recordId", recordId)
                .getResultList();
        
    }
    
}
