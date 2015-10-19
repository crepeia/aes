/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.PageNavigation;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author thiagorizuti
 */
@ManagedBean(name = "pageNavigationController")
@SessionScoped
public class PageNavigationController extends BaseController<PageNavigation> {

    private PageNavigation pageNavigation;

    public PageNavigationController() {
        try {
            this.daoBase = new GenericDAO<PageNavigation>(PageNavigation.class);
        } catch (NamingException ex) {
            Logger.getLogger(PageNavigationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String saveNavigation() {
        pageNavigation = new PageNavigation();
        pageNavigation.setIp(getIpAdress());
        pageNavigation.setTimeStamp(new Date());
        pageNavigation.setUrl(getURL());
        pageNavigation.setUser(getUser());
        try {
            this.daoBase.insert(pageNavigation, this.getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(PageNavigationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public User getUser(){
        return getLoggedUser();
    }

    public String getIpAdress() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    public String getURL(){
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = ((HttpServletRequest) request).getRequestURI();
	url = url.substring( url.lastIndexOf('/') + 1 );
        return url;
        
    }
    
    

}
