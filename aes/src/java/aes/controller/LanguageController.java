package aes.controller;

import aes.model.User;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named("languageController")
@SessionScoped
public class LanguageController extends BaseController<User> {

    private Map<String, String> languages = new LinkedHashMap<>();
    
    @Inject
    private UserController userController;
    
    private Locale locale;
    
    @PostConstruct
    public void init(){
        languages.put("English", "en");
        languages.put("Español", "es");
        languages.put("Português", "pt");   
        locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        //FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);        
    }
    
    
    
    public void setLanguage(String language) {
        locale = new Locale(language);
        userController.getUser().setPreferedLanguage(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);

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
