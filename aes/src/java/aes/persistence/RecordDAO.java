/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Record;
import aes.model.User;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author patri
 */
public class RecordDAO extends GenericDAO<Record>{
    
    public RecordDAO(EntityManager entityManager) throws NamingException {
        super(Record.class);
        this.setEntityManager(entityManager);
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
 
    public Record create(Long userId) throws SQLException {

            Record entity = new Record();
            entity.setUser(getEntityManager().find(User.class, userId));
            entity.setDailyGoal(0);
            entity.setWeeklyGoal(0);
            super.insert(entity);

            return entity;

    }


    public Record edit(Record entity) throws SQLException {
       // String userEmail = securityContext.getUserPrincipal().getName();
       // User u = em.find(User.class, entity.getUser().getId());
       // if (u.getEmail().equals(userEmail)) {
            super.insertOrUpdate(entity);
       // }

        return entity;
    }


    public Record findByUserId(Long userId) {

        try {
            Record rec = (Record) getEntityManager().createQuery("SELECT r FROM Record r WHERE r.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return rec;
        } catch (NoResultException e) {
            return null;
        }
    }
    
}
