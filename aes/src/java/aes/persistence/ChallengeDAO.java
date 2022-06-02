/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Challenge;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class ChallengeDAO extends GenericDAO<Challenge> {
        
    public ChallengeDAO(EntityManager entityManager) throws NamingException {
        super(Challenge.class);
        this.setEntityManager(entityManager);

    }
    

    public List<Challenge> findAll() throws SQLException {
        return super.list();
    }
    
    public List<Challenge> findAllByType(Challenge.ChallengeType ct) {
        return getEntityManager().createQuery("SELECT c FROM Challenge c WHERE c.type=:type")
                .setParameter("type", ct)
                .getResultList();
    }
}
