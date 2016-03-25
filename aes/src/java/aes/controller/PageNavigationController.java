package aes.controller;

import aes.model.PageNavigation;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
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

    @ManagedProperty(value = "#{userController}")
    private UserController userController;

    @PostConstruct
    public void init() {
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

    public User getUser() {
        if (userController.isLoggedIn()) {
            return userController.getUser();
        }else{
            return null;
        }
    }

    private String getIpAdress() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String getURL() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.lastIndexOf('/') + 1);
        return url;

    }

}
