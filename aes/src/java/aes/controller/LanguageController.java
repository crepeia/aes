/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiagorizuti
 */
@ManagedBean(name = "languageController")
@SessionScoped
public class LanguageController extends BaseController<User> {

    private Locale locale;
    private Map<String, String> languages = new LinkedHashMap<String, String>();
    private ResourceBundle bundle;

    public LanguageController() {
        try {
            daoBase = new GenericDAO(User.class);
        } catch (NamingException ex) {
            Logger.getLogger(LanguageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (loggedUser() && getLoggedUser().getLanguage() != null) {
            locale = new Locale(getLoggedUser().getLanguage());
        } else {
            locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        }
        languages.put("English", "en");
        languages.put("Español", "es");
        languages.put("Português", "pt");
    }

    public String getText(String key) {
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", locale);
        return bundle.getString(key);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public void setLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        if (loggedUser()) {
            getLoggedUser().setLanguage(language);
            try {
                new GenericDAO(User.class).insertOrUpdate(getLoggedUser(), getEntityManager());
            } catch (NamingException ex) {
                Logger.getLogger(LanguageController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(LanguageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Map<String, String> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, String> languages) {
        this.languages = languages;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

}
