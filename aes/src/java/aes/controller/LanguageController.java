package aes.controller;

import aes.model.User;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "languageController")
@SessionScoped
public class LanguageController extends BaseController<User> {

    private Map<String, String> languages = new LinkedHashMap<String, String>();
    
    @ManagedProperty(value = "#{userController}")
    private UserController userController;
    
    private Locale locale;
    
    @PostConstruct
    public void init(){
        languages.put("English", "en");
        languages.put("Español", "es");
        languages.put("Português", "pt");       
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);        
    }
    
    
    
    public void setLanguage(String language) {
        userController.getUser().setPreferedLanguage(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
        if (userController.isLoggedIn()) {
            userController.save();
        }
    }
    
    public String getLanguage(){
        return getLocale().getLanguage();
    }
    
    public Locale getLocale() {
        if(getUser().getPreferedLanguage() != null){            
            locale = new Locale(getUser().getPreferedLanguage());
        }else{
            locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
            userController.getUser().setPreferedLanguage(locale.getLanguage());
        }
        return locale;
    }
    
    public User getUser(){
       return userController.getUser();
    }
    
    public Map<String, String> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, String> languages) {
        this.languages = languages;
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    } 
    
}
