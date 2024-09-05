package aes.controller;

import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.AppServletContextListener;
import com.sun.faces.component.visit.FullVisitContext;
import java.io.Serializable;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class BaseController<T> implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    protected GenericDAO<T> daoBase;

    public BaseController() {
    }

    public GenericDAO<T> getDaoBase() {
        return daoBase;
    }

    public Map<String, String> getParameterMap() {
        return (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
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
    
    public boolean loggedUser() {
        return getLoggedUser() != null;
    }

    public User getLoggedUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
    }

    public String getRecaptchaKey() {
        return AppServletContextListener.getServletContext().getInitParameter("recaptchaKey");
    }

}
