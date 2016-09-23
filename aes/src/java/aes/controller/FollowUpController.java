/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.FollowUp;
import aes.model.Satisfaction;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.io.IOException;
import static java.lang.Math.toIntExact;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiago
 */
@ManagedBean(name = "followUpController")
@ViewScoped
public class FollowUpController extends BaseController<FollowUp> {

    private FollowUp followUp;
    private GenericDAO daoUser;
    private long id;
    private int week;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        id = params.get("hid") != null ? Long.parseLong(params.get("hid")) : 0;
        week = params.get("hwk") != null ? Integer.parseInt(params.get("hwk")) : 0;
        System.out.println(id + " " + week);
        try {
            daoBase = new GenericDAO<>(FollowUp.class);
            daoUser = new GenericDAO<>(User.class);
        } catch (NamingException ex) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FollowUp getFollowUp() {
        if (followUp == null) {
            followUp = new FollowUp();
        }
        return followUp;
    }

    private User getUser() {
        System.out.println("id: " + id + " % 1357 " + id % 1357);
        try {
            if (id % 1357 != 0) {
                Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, "User answering follow up form with invalid id: " + id);
                return null;
            } else {
                id = id / 1357;
                List users = daoUser.list("id", id, getEntityManager());
                if (users.isEmpty()) {
                    Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, "User answering follow up form with invalid id: " + id);
                    return null;
                } else {
                    return (User) users.get(0);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Integer getWeek() {
        System.out.println("week: " + week + " % 1357 " + week % 1357);
        if (week % 1357 != 0) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, "User answering satisfaction form with invalid week: " + week);
            return null;
        } else {
            return week / 1357;

        }
    }

    public void save() {
        try {
            getFollowUp().setDateAnswered(new Date());
            getFollowUp().setUser(getUser());
            getFollowUp().setWeekCount(getWeek());
            daoBase.insert(getFollowUp(), getEntityManager());
            followUp = null;
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        } catch (SQLException | IOException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
