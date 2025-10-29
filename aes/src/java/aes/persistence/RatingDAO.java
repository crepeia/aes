/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Rating;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Leonorico
 */
public class RatingDAO extends GenericDAO<Rating> {
    
    public RatingDAO() throws NamingException {
        super(Rating.class);
    }
    
    public List<Rating> listRatingByUserIdAndItemId(Long userId, Long itemId, EntityManager entityManager) throws SQLException {

        try {
            Query query;
            
            if(userId != null && itemId != null) {
                query = entityManager.createQuery("select r from Rating r where r.user.id = :userId and r.item.id = :itemId order by r.dateRated desc");
                query.setParameter("userId", userId);
                query.setParameter("itemId", itemId);
            } else {
                query = entityManager.createQuery("select r from Rating r where r.user.id is Null and r.item.id is Null");
            }
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
}
