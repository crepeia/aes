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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author patri
 */
public class ChallengeDAO extends GenericDAO<Challenge> {
        
    public ChallengeDAO() throws NamingException {
        super(Challenge.class);
    }
    

    public List<Challenge> findAll(EntityManager entityManager) throws SQLException {
        return super.list(entityManager);
    }
    
    public List<Challenge> findAllByType(Challenge.ChallengeType ct, EntityManager entityManager) {
        return entityManager.createQuery("SELECT c FROM Challenge c WHERE c.type=:type")
                .setParameter("type", ct)
                .getResultList();
    }
}
