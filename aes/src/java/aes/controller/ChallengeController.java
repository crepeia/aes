package aes.controller;

import aes.model.Challenge;
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
@Named("challengeController")
@RequestScoped
public class ChallengeController extends BaseController<Challenge>{

    private Challenge challenge = new Challenge();
    
    private Long id;
    private String title;
    private String description;
    private Integer base_value;
    private Float modifier;

    
    private Challenge.ChallengeType type;

    
    List<Challenge> challengeList;
    
    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<>(Challenge.class);
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
    
    public Challenge getChallenge() {
        return challenge;
    }
    
    public void create() {
        try {
            challengeList = this.getDaoBase().list("title", challenge.getTitle(), getEntityManager());

            if (!challengeList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Desafio já cadastrado.", null));
            } else {
                daoBase.insert(getChallenge(), getEntityManager());
                challenge = null;
                FacesContext.getCurrentInstance().getExternalContext().redirect("lista-desafios.xhtml");
            }
            
        } catch (SQLException ex) {
                Logger.getLogger(ChallengeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String redirectEdit(){
        return "editar-desafio.xhtml";
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }
    
    public void edit(){
        Challenge newChallenge = getChallenge();
        try {
            getDaoBase().update(newChallenge, getEntityManager());
            FacesContext.getCurrentInstance().getExternalContext().redirect("lista-desafios.xhtml");
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(ChallengeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void delete(){
        Challenge deleteChallenge = getChallenge();
        try {
            getDaoBase().delete(deleteChallenge, getEntityManager());
            FacesContext.getCurrentInstance().getExternalContext().redirect("lista-desafios.xhtml");
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(ChallengeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<Challenge> challengeList(){
        try {
            challengeList = this.getDaoBase().list(getEntityManager());
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(ChallengeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return challengeList;
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
    
    public Challenge.ChallengeType[] getChallengeTypes(){
        return Challenge.ChallengeType.values();
      }
    
    public Integer getBase_value() {
        return base_value;
    }

    public void setBase_value(Integer base_value) {
        this.base_value = base_value;
    }

    public Float getModifier() {
        return modifier;
    }

    public void setModifier(Float modifier) {
        this.modifier = modifier;
    }

    public Challenge.ChallengeType getType() {
        return type;
    }

    public void setType(Challenge.ChallengeType type) {
        this.type = type;
    }
}
