/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import wati.persistence.GenericDAO;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author hedersb
 */
public abstract class BaseController<T> implements Serializable {

	@PersistenceContext
	private EntityManager entityManager = null;

	protected GenericDAO<T> daoBase;

	public BaseController() {
	}

//	public BaseController(Class<T> cls) {
//
//		try {
//			this.daoBase = new GenericDAO<T>(cls);
//		} catch (NamingException ex) {
//			String message = "Ocorreu um erro inesperado.";
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
//			Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//		}
//
//	}

//	/**
//	 * @return the entityManager
//	 */
//	public EntityManager getEntityManager() {
//		try {
//			if (this.entityManager == null) {
//				this.entityManager = Persistence.createEntityManagerFactory("watiPU").createEntityManager();
//				System.out.println("entity");
//				System.out.println(this.entityManager);
//			}
//
//		} catch (Exception e) {
//			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, e);
//		}
//		return this.entityManager;
//	}
//
//	/**
//	 * @param entityManager the entityManager to set
//	 */
//	public void setEntityManager(EntityManager entityManager) {
//		this.entityManager = entityManager;
//	}

	/**
	 * @return the daoBase
	 */
	public GenericDAO<T> getDaoBase() {
		return daoBase;
	}

	/**
	 *
	 * @return ParameterMap
	 */
	public Map<String, String> getParameterMap() {
		return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	}

//	/**
//	 *
//	 * @return Locale
//	 */
//	protected Locale getLocale() {
//		return FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{languageBean.language}", Locale.class);
//	}

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

}
