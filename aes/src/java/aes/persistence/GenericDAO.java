package aes.persistence;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import org.hibernate.CacheMode;

public class GenericDAO<T> implements Serializable {

	public final int E = 0, OU = 1;
	protected Class<T> classe;
	private UserTransaction transaction;
        @PersistenceContext(unitName = "aesPU")
        private EntityManager entityManager;


	public GenericDAO(Class<T> classe) throws NamingException {
		this.classe = classe;

		this.transaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");

	}
        
     public EntityManager getEntityManager() {
        return entityManager;
    }

      public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

	public Class<T> getClasse() {
		return classe;
	}

	public void setClasse(Class<T> classe) {
		this.classe = classe;
	}

	public void insert(T objeto, EntityManager entityManager) throws SQLException {
		try {

			this.transaction.begin();
			entityManager.persist(objeto);
			this.transaction.commit();

		} catch (Exception erro) {
			Logger.getLogger(GenericDAO.class.getName()).log(Level.SEVERE, erro.getMessage(), erro);
			throw new SQLException(erro);
		}
	}

	public void delete(T objeto, EntityManager entityManager) throws SQLException {
		try {

			T objetoExcluir;

			Class classeObj = classe;
			while (!classeObj.getSuperclass().equals(Object.class)) {
				classeObj = classeObj.getSuperclass();
			}

			this.transaction.begin();
			//verificar como saber o Id da classe automaticamente - olhar em outro metodo tbm
			objetoExcluir = entityManager.find(classe, Long.parseLong(classe.getMethod("getId", null).invoke(objeto, null).toString()));

			entityManager.remove(objetoExcluir);
			this.transaction.commit();

		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public void delete(List<T> list, EntityManager entityManager) throws SQLException {
		try {

			T objetoExcluir;

			this.transaction.begin();
			for (T objeto : list) {
				Class classeObj = classe;
				while (!classeObj.getSuperclass().equals(Object.class)) {
					classeObj = classeObj.getSuperclass();
				}

				//verificar como saber o Id da classe automaticamente - olhar em outro metodo tbm
				objetoExcluir = entityManager.find(classe, Long.parseLong(classe.getMethod("getId", null).invoke(objeto, null).toString()));

				entityManager.remove(objetoExcluir);
			}
			this.transaction.commit();

		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public void update(T objeto, EntityManager entityManager) throws SQLException {
		try {

			this.transaction.begin();
			entityManager.merge(objeto);
			this.transaction.commit();

		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public void update(List<T> lista, EntityManager entityManager) throws SQLException {
		try {

			Class classeObj = classe;
			while (!classeObj.getSuperclass().equals(Object.class)) {
				classeObj = classeObj.getSuperclass();
			}

			int id;
			this.transaction.begin();
			for (T objeto : lista) {
				id = Integer.parseInt(classe.getMethod("getId", null).invoke(objeto, null).toString());
				if (id > 0) {
					entityManager.merge(objeto);
				} else {
					entityManager.persist(objeto);
				}
			}
			this.transaction.commit();

		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public void insertOrUpdate(T object, EntityManager entityManager) throws SQLException {
        
		try {

			Class classeObj = classe;
			while (!classeObj.getSuperclass().equals(Object.class)) {
				classeObj = classeObj.getSuperclass();
			}

			this.transaction.begin();
			long id = Long.parseLong(classe.getMethod("getId", null).invoke(object, null).toString());
			if (id > 0) {
				entityManager.merge(object);
			} else {
				entityManager.persist(object);
			}
			this.transaction.commit();

		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> list(EntityManager entityManager) throws SQLException {
		try {

			Query query = entityManager.createQuery("select obj from " + classe.getSimpleName() + " obj");
			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			return query.getResultList();
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public List<T> list(String campoStr, Object objeto, EntityManager entityManager) throws SQLException {

		try {

			campoStr = campoStr.substring(0, 1).toLowerCase() + campoStr.substring(1);

			Query query;
			if (objeto != null) {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj where obj." + campoStr + " = :objeto");
				query.setParameter("objeto", objeto);
			} else {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj where obj." + campoStr + " is Null");
			}
			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			return query.getResultList();
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}
        
        public List<T> listNotNull(String campoStr, EntityManager entityManager) throws SQLException {

		try {

			campoStr = campoStr.substring(0, 1).toLowerCase() + campoStr.substring(1);

			Query query;
			
                        query = entityManager.createQuery("select obj from " + classe.getSimpleName()
                                        + " obj where obj." + campoStr + " is not Null");

			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			return query.getResultList();
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}
        
        public List<T> listOrdered(String campoStr, EntityManager entityManager) throws SQLException {

		try {

			campoStr = campoStr.substring(0, 1).toLowerCase() + campoStr.substring(1);

			Query query;
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj order by obj." + campoStr);
			
			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			return query.getResultList();
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}
        
        public List<T> listOrdered(String fieldCompare, Object objectCompare, String fieldOrder, EntityManager entityManager) throws SQLException {

		try {

			fieldCompare = fieldCompare.substring(0, 1).toLowerCase() + fieldCompare.substring(1);
                        fieldOrder = fieldOrder.substring(0, 1).toLowerCase() + fieldOrder.substring(1);


			Query query;
			if (objectCompare != null) {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj where obj." + fieldCompare + " = :objectCompare order by obj." + fieldOrder);
				query.setParameter("objectCompare", objectCompare);
			} else {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj where obj." + fieldCompare + " is Null order by obj." + fieldOrder);
			}
			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			return query.getResultList();
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public T listOnce(String campoStr, Object objeto, EntityManager entityManager) throws SQLException {
		try {
			campoStr = campoStr.substring(0, 1).toLowerCase() + campoStr.substring(1);

			Query query;
			if (objeto != null) {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj where obj." + campoStr + " = :objeto");
				query.setParameter("objeto", objeto);
			} else {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName()
						+ " obj where obj." + campoStr + " is Null");
			}
			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			Object obj = query.getSingleResult();
			return obj == null ? null : (T) obj;
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}

	public Date getDate(EntityManager entityManager) throws SQLException {
		Date data = null;
		try {
			Query q = entityManager.createNativeQuery("Select now()");
			// GetSingleResult neste caso est� retornando um Vector, n�o sei o porque
			// E nesse Vector vem um Date com a data e hora do banco de dados
			data = (Date) ((List) q.getSingleResult()).get(0);
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
		return data;
	}

	public List<T> listContain(String campoStr, Object objeto, boolean in, EntityManager entityManager) throws SQLException {
		try {

			campoStr = campoStr.substring(0, 1).toLowerCase() + campoStr.substring(1);

			String inStr = in ? " 1 " : " 0 ";

			Query query;
			if (objeto == null) {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName() + " obj");
			} else {
				query = entityManager.createQuery("select obj from " + classe.getSimpleName() + " obj where"
						+ "(SELECT COUNT(field) FROM obj." + campoStr + " field WHERE field=:objeto)=" + inStr);
				query.setParameter("objeto", objeto);
			}
			//query.setHint("toplink.refresh", "true");
                        query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
                        query.setHint("org.hibernate.cacheable", true);
			return query.getResultList();
		} catch (Exception erro) {
			throw new SQLException(erro);
		}
	}
        
        
    public List<T> findRange(int[] range, EntityManager entityManager) {
        Query query = entityManager.createQuery("select obj from " + classe.getSimpleName() + " obj");
        query.setMaxResults(range[1] - range[0] + 1);
        query.setFirstResult(range[0]);
        
        //javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        //cq.select(cq.from(T));
       // javax.persistence.Query q = entityManager.createQuery(cq);
       // q.setMaxResults(range[1] - range[0] + 1);
        //q.setFirstResult(range[0]);
        return query.getResultList();
    }
    
       public T find(Object id, EntityManager entityManager) {
        return entityManager.find(classe, id);
    }
    
        public int count(EntityManager entityManager) {
        javax.persistence.criteria.CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(classe);
        cq.select(entityManager.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = entityManager.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
}
