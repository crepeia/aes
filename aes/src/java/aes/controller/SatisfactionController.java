/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Satisfaction;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.NamingException;

/**
 *
 * @author thiago
 */
@Named("satisfactionController")
@ViewScoped
public class SatisfactionController extends BaseController<Satisfaction> {

    private Satisfaction satisfaction;
    private GenericDAO daoUser;
    private long id;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap(); 
        id = params.get("hid") != null ? Long.parseLong(params.get("hid")) : 0;
        try {
            daoBase = new GenericDAO<Satisfaction>(Satisfaction.class);
            daoUser = new GenericDAO<User>(User.class);
            daoBase.setEntityManager(getEntityManager());
            daoUser.setEntityManager(getEntityManager());
        } catch (NamingException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Satisfaction getSatisfaction() {
        if (satisfaction == null) {
            satisfaction = new Satisfaction();
        }
        return satisfaction;
    }

    private User getUser() {
        try {
            if (id % 1357 != 0) {
                Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, "User answering satisfaction form with invalid id: " );
                return null;
            } else {
                id = id / 1357;
                List users = daoUser.list("id", id);
                if (users.isEmpty()) {
                    Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, "User answering satisfaction form with invalid id: ");
                    return null;
                } else {
                    return (User) users.get(0);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void save() {
        try {
            getSatisfaction().setDateAnswered(new Date());
            getSatisfaction().setUser(getUser());
            daoBase.insert(getSatisfaction());
            satisfaction = null;
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        } catch (SQLException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
