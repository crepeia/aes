/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Record;
import aes.model.User;
import aes.service.RecordFacadeREST;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author patri
 */
public class RecordDAO extends GenericDAO<Record>{
    
    public RecordDAO() throws NamingException {
        super(Record.class);
    }
    

    /*public Record createRecord(Record entity, EntityManager entityManager) throws SQLException {
        //String userEmail = securityContext.getUserPrincipal().getName();
        //User u = em.find(User.class, entity.getUser().getId());
        //if (!u.getEmail().equals(userEmail)) {
          //  return Response.status(Response.Status.UNAUTHORIZED).build();
        //}
            super.insert(entity, entityManager);
            return entity;
            //return Response.ok().entity(created).build();

    }
*/
 
    public Record create(Long userId, EntityManager entityManager) throws SQLException {

            Record entity = new Record();
            entity.setUser(entityManager.find(User.class, userId));
            entity.setDailyGoal(0);
            entity.setWeeklyGoal(0);
            super.insert(entity, entityManager);

            return entity;

    }


    public Record edit(Record entity, EntityManager entityManager) throws SQLException {
       // String userEmail = securityContext.getUserPrincipal().getName();
       // User u = em.find(User.class, entity.getUser().getId());
       // if (u.getEmail().equals(userEmail)) {
            super.insertOrUpdate(entity, entityManager);
       // }

        return entity;
    }


    public Record find(Long userId, EntityManager entityManager) {

        try {
            Record rec = (Record) entityManager.createQuery("SELECT r FROM Record r WHERE r.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return rec;
        } catch (NoResultException e) {
            return null;
        }
    }
    
}
