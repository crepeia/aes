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
public class LanguageController extends BaseController<Object> {

    private Locale locale;
    private Map<String, String> languages = new LinkedHashMap<String, String>();

    public LanguageController() {
        locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        languages.put("English", "en");
        languages.put("Español", "es");
        languages.put("Português", "pt");
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        if(loggedUser()){
            locale = new Locale(getLoggedUser().getPreferedLanguage());
            FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        }
        return locale.getLanguage();
    }

    public void setLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        getLoggedUser().setPreferedLanguage(language);
        try {
            new GenericDAO(User.class).insertOrUpdate(getLoggedUser(), getEntityManager());
        } catch (NamingException ex) {
            Logger.getLogger(LanguageController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(LanguageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Map<String, String> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, String> languages) {
        this.languages = languages;
    }

}
