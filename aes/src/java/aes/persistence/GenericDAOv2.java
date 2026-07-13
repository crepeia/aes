package aes.persistence;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author luansb
 */

@Dependent
public abstract class GenericDAOv2<T> implements Serializable {
    @PersistenceContext(unitName = "aesPU")
    protected EntityManager em;
    
    private final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    protected GenericDAOv2() {
        this.entityClass =
            (Class<T>) ((ParameterizedType)
            getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }
    
    public void persist(T entity) {
        em.persist(entity);
    }
    
    public T merge(T entity) {
        return em.merge(entity);
    }
    
    public void remove(T entity) {
        em.remove(
            em.contains(entity)
            ? entity
            : em.merge(entity)
        );
    }
    
    public T find(Object id) {
        return em.find(entityClass, id);
    }
    
    public long count() {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq =
                cb.createQuery(Long.class);

        Root<T> root = cq.from(entityClass);

        cq.select(cb.count(root));

        return em.createQuery(cq).getSingleResult();
    }
    
    public List<T> list() {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> cq =
                cb.createQuery(entityClass);

        cq.from(entityClass);

        return em.createQuery(cq).getResultList();
    }
    
    private String normalizeField(String field){
        return field.substring(0,1).toLowerCase()+field.substring(1);
    }
    
    public List<T> list(String field, Object value) {
        field = normalizeField(field);
        
        String jpql =
            "SELECT e FROM "
            + entityClass.getSimpleName()
            + " e WHERE e."
            + field
            + (value == null ? " IS NULL" : " = :value");

        TypedQuery<T> query =
            em.createQuery(jpql, entityClass);

        if(value != null)
            query.setParameter("value", value);

        return query.getResultList();
    }
    
    public List<T> listNotNull(String field) {
        String jpql =
            "SELECT e FROM "
            + entityClass.getSimpleName()
            + " e WHERE e."
            + field
            + " IS NOT NULL";

        return em.createQuery(jpql, entityClass).getResultList();
    }
    
    public List<T> listOrdered(String field) {
        String jpql =
            "SELECT e FROM "
            + entityClass.getSimpleName()
            + " e ORDER BY e."
            + field;

        return em.createQuery(jpql, entityClass).getResultList();
    }
    
    public T insertOrUpdate(T entity) {
        return em.merge(entity);
    }
}
