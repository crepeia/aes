/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AgendaAvailable;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Leonorico
 */
public class AgendaAvailableDAO extends GenericDAO<AgendaAvailable> {
    
    public AgendaAvailableDAO() throws NamingException {
        super(AgendaAvailable.class);
    }
    
    public List<AgendaAvailable> find(Long id, EntityManager entityManager) throws SQLException {
        Query query;
        try {
            query = entityManager.createQuery("select av from AgendaAvailable av where av.id = :id");
            query.setParameter("id", id);
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
    
    public List<AgendaAvailable> listByConsultant(Long consultantId, EntityManager em) throws SQLException {
        try {
            Query query = em.createQuery(
                    "SELECT av FROM AgendaAvailable av "
                            + "JOIN av.user u "
                            + "WHERE u.id IN ("
                            + "SELECT u2.id "
                            + "FROM User u2 "
                            + "WHERE u2.relatedConsultant.id = :consultantId)");
            query.setParameter("consultantId", consultantId);
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }    
}
