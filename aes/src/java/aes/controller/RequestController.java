/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Evaluation;
import aes.model.Item;
import aes.model.Rating;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author thiago
 */
@RequestScoped
@Named("requestController")
public class RequestController extends BaseController<User> {

    private final String key = "11VzuKzy5k";
    private GenericDAO daoUser;
    private GenericDAO daoEvaluation;
    private GenericDAO daoRating;
    private GenericDAO daoItem;

    @PostConstruct
    public void init() {
        try {
            daoUser = new GenericDAO<User>(User.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getAppRequest() {
        try {
            daoEvaluation = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        }

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request.getParameter("key").contains(key)) {
            try {
                User user = null;
                String email = request.getParameter("email");
                if (email != null) {
                    List<User> users = daoUser.list("email", email, getEntityManager());
                    if (!users.isEmpty()) {
                        user = users.get(0);
                    }
                }
                if (user == null) {
                    user = new User();
                }
                user.setEmail(email);
                user.setBirth(Long.parseLong(request.getParameter("birth")));
                user.setGender(request.getParameter("birth").charAt(0));
                user.setDrink(Boolean.valueOf(request.getParameter("drink")));
                daoUser.insertOrUpdate(user, getEntityManager());

                Evaluation evaluation = new Evaluation();
                evaluation.setUser(user);
                evaluation.setDateCreated(Long.parseLong(request.getParameter("dateCreated")));
                evaluation.setAudit1(Integer.valueOf(request.getParameter("audit1")));
                evaluation.setAudit2(Integer.valueOf(request.getParameter("audit2")));
                evaluation.setAudit3(Integer.valueOf(request.getParameter("audit3")));
                evaluation.setAudit4(Integer.valueOf(request.getParameter("audit4")));
                evaluation.setAudit5(Integer.valueOf(request.getParameter("audit5")));
                evaluation.setAudit6(Integer.valueOf(request.getParameter("audit6")));
                evaluation.setAudit7(Integer.valueOf(request.getParameter("audit7")));
                evaluation.setAudit8(Integer.valueOf(request.getParameter("audit8")));
                evaluation.setAudit9(Integer.valueOf(request.getParameter("audit9")));
                evaluation.setAudit10(Integer.valueOf(request.getParameter("audit10")));
                evaluation.setMonday(Integer.valueOf(request.getParameter("monday")));
                evaluation.setTuesday(Integer.valueOf(request.getParameter("tuesday")));
                evaluation.setWednesday(Integer.valueOf(request.getParameter("wednesday")));
                evaluation.setThursday(Integer.valueOf(request.getParameter("thursday")));
                evaluation.setFriday(Integer.valueOf(request.getParameter("friday")));
                evaluation.setSaturday(Integer.valueOf(request.getParameter("saturday")));
                evaluation.setSunday(Integer.valueOf(request.getParameter("sunday")));
                daoEvaluation.insertOrUpdate(evaluation, getEntityManager());
            } catch (SQLException ex) {
                Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return request.getParameter("key");
    }

    public String getEmailRatingRequest() {
        try {
            daoItem = new GenericDAO<Item>(Item.class);
            daoRating = new GenericDAO<Rating>(Rating.class);
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String itemName = request.getParameter("itemName") != null ? request.getParameter("itemName") : " ";
            String userEmail = request.getParameter("userEmail") != null ? request.getParameter("userEmail") : " ";
            Boolean rate = request.getParameter("yes") != null;
            User user = getUser(userEmail);
            Item item = getItem(itemName);
            if (user == null){
                return "no user";
            }        
            if (item == null){
                item = new Item();
                item.setName(itemName);
                item.setType("message");
                daoItem.insert(item, getEntityManager());
            }
            Rating rating = getRating(user, item);
            if(rating == null){
               return "no rating"; 
            }
            rating.setDateRated(new Date());
            rating.setRelevant(rate);
            daoRating.insertOrUpdate(rating, getEntityManager());
            //FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            String message = getString("ratings.email.thanks", user);
            return message;

        } catch (NamingException | SQLException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "exception";
    }
    
    public Rating getRating(User user, Item item) { 
        try {
            Rating rating = null;
            List<Rating> ratings = daoRating.list("user", user, getEntityManager());
            if (!ratings.isEmpty()) {
                for (Rating r : ratings) {
                    if (r.getItem() != null && r.getItem().getId() == item.getId()) {
                        rating = r;
                    }
                }
            }
            if (rating == null) {
                rating = new Rating();
                rating.setUser(user);
                rating.setItem(item);
                rating.setDateRated(new Date());
            }
            
            return rating;
        } catch (SQLException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public User getUser(String userEmail) {
        try {
            List users = daoUser.list("email", userEmail, getEntityManager());
            if (users.isEmpty()) {
                return null;
            } else {
                return (User) users.get(0);
            }       
        } catch (SQLException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

   public Item getItem(String itemName){
       try {
            List items = daoItem.list("name", itemName, getEntityManager());
            if (items.isEmpty()) {
                return null;
            } else {
                return (Item) items.get(0);
            }       
        } catch (SQLException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
   }
   
   private String getString(String key, User user){
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }

}
