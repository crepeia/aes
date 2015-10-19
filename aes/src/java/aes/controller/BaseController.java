/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.User;
import aes.persistence.GenericDAO;
import com.sun.faces.component.visit.FullVisitContext;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
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

    public boolean loggedUser() {
        return getLoggedUser() != null;
    }

    public User getLoggedUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
    }
    
    public UIComponent getComponent(final String id) {

    FacesContext context = FacesContext.getCurrentInstance(); 
    UIViewRoot root = context.getViewRoot();
    final UIComponent[] found = new UIComponent[1];

    root.visitTree(new FullVisitContext(context), new VisitCallback() {     
        @Override
        public VisitResult visit(VisitContext context, UIComponent component) {
            if(component.getId().equals(id)){
                found[0] = component;
                return VisitResult.COMPLETE;
            }
            return VisitResult.ACCEPT;              
        }
    });

    return found[0];

    }   
    
    public String getText(String key) {
        User user = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
        ResourceBundle bundle;
        Locale locale;
        if (user != null) {
            locale = new Locale(user.getPreferedLanguage());
        } else {
            locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        }
        bundle = PropertyResourceBundle.getBundle("wati.utility.messages", locale);
        return bundle.getString(key);
    }

}
