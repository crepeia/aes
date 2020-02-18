package aes.controller;

import aes.model.Tip;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.Encrypter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.application.FacesMessage;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author bruno
 */
@ManagedBean(name = "tipController")
@ViewScoped
public class TipController extends BaseController<Tip>{

    private Tip tip;
    private String title;
    private String description;
    
    public TipController() {
    }
    
    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<>(Tip.class);
            tip = new Tip();
        } catch (NamingException ex) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Tip getTip() {
        if (tip == null) {
            tip = new Tip();
        }
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
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            }
            
        } catch (SQLException ex) {
                Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public void edit(){
        Tip tip = getTip();
        try {

            getDaoBase().update(tip, getEntityManager());

        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, null, ex);

        }
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
