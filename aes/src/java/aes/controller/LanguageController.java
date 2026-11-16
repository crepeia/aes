package aes.controller;

import aes.model.User;
import aes.utility.MessageResources;
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
        locale = MessageResources.resolveLocale(FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getLanguage());
        //FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);        
    }
    
    
    
    public void setLanguage(String language) {
        locale = MessageResources.resolveLocale(language);
        userController.getUser().setPreferedLanguage(locale.getLanguage());
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
            locale = MessageResources.resolveLocale(getUser().getPreferedLanguage());
        }else{
            locale = MessageResources.resolveLocale(FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getLanguage());
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
