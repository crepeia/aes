/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Item;
import aes.model.Rating;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;


/**
 *
 * @author thiago
 */
@Named("pageRatingController")
@ViewScoped
public class PageRatingController extends BaseController<Rating> {

    private GenericDAO daoItem;
    private Rating rating;
    private Item page;
    
    @Inject
    private UserController userController;
    

    public PageRatingController() {
        try {
            daoBase = new GenericDAO<Rating>(Rating.class);
            daoItem = new GenericDAO<Item>(Item.class);
            daoBase.setEntityManager(getEntityManager());
            daoItem.setEntityManager(getEntityManager());

        } catch (NamingException ex) {
            Logger.getLogger(PageRatingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Rating getPageRating() {
        if (rating == null) {
            try {
                if (getUser() != null) {
                    List<Rating> ratings = daoBase.list("user", getUser());
                    if (!ratings.isEmpty()) {
                        for (Rating r : ratings) {
                            if (r.getItem() != null && r.getItem().getName().contains(getURL())) {
                                rating = r;
                            }
                        }
                    }
                }
                if (rating == null) {
                    rating = new Rating();
                    rating.setUser(getUser());
                    rating.setItem(getPage());
                }
            } catch (SQLException ex) {
                Logger.getLogger(PageRatingController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rating;
    }

    public Item getPage() {
        if (page == null) {
            try {
                List<Item> pages = daoItem.list("name", getURL());
                if (pages.isEmpty()) {
                    page = new Item();
                    page.setName(getURL());
                    page.setType("page");
                    daoItem.insert(page);
                } else {
                    page = pages.get(0);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PageRatingController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return page;
    }

    public void ratePage(ActionEvent event) {
        try {
            String action = (String) event.getComponent().getAttributes().get("button");
            if (action.contains("like")) {
                getPageRating().setRelevant(true);
            } if(action.contains("unlike")) {
                getPageRating().setRelevant(false);
            }
            getPageRating().setDateRated(new Date());
            daoBase.insertOrUpdate(getPageRating());
        } catch (SQLException ex) {
            Logger.getLogger(PageRatingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getURL() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.indexOf('-') + 1);
        return url;
    }

    public User getUser() {
        return userController.getUser();
    }

    public String getImageLike() {
        if (getPageRating().getRelevant() == null) {
            return "images/like.png";
        } else if (!getPageRating().getRelevant()) {
            return "images/like.png";
        } else {
            return "images/like-pressed.png";
        }
    }

    public String getImageUnlike() {
        if (getPageRating().getRelevant() == null) {
            return "images/unlike.png";
        } else if (getPageRating().getRelevant()) {
            return "images/unlike.png";
        } else {
            return "images/unlike-pressed.png";
        }
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }
    
   
}
