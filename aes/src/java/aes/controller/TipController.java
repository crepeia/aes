package aes.controller;

import aes.model.Tip;
import aes.persistence.GenericDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;

import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;

/**
 *
 * @author bruno
 */
@Named("tipController")
@RequestScoped
public class TipController extends BaseController<Tip>{

    private Tip tip = new Tip();
    
    private Long id;

  
    private String title;
    private String description;

    
    List<Tip> tipList;
    
    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<>(Tip.class);
        } catch (NamingException ex) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Tip getTip() {
        return tip;
    }
    
    public void create() {
        try {
            List<Tip> tipList = this.getDaoBase().list("title", tip.getTitle(), getEntityManager());

            if (!tipList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Dica já cadastrada.", null));
            } else {
                daoBase.insert(getTip(), getEntityManager());
                tip = null;
                FacesContext.getCurrentInstance().getExternalContext().redirect("lista-dicas.xhtml");
            }
            
        } catch (SQLException ex) {
                Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String redirectEdit(){
        return "editar-dica.xhtml";
    }

    public void setTip(Tip tip) {
        this.tip = tip;
    }
    
    public void edit(){
        Tip newtip = getTip();
        try {
            getDaoBase().update(newtip, getEntityManager());
            FacesContext.getCurrentInstance().getExternalContext().redirect("lista-dicas.xhtml");
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void delete(){
        Tip deleteTip = getTip();
        try {
            getDaoBase().delete(deleteTip, getEntityManager());
            FacesContext.getCurrentInstance().getExternalContext().redirect("lista-dicas.xhtml");
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<Tip> tipList(){
        try {
            tipList = this.getDaoBase().list(getEntityManager());
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tipList;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
