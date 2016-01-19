/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.DailyLog;
import aes.model.KeepResults;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiago
 */
@ManagedBean(name = "keepResultsController")
@SessionScoped
public class KeepResultsController extends BaseController<KeepResults>{
    
    private KeepResults keepResults;
    private DailyLog dailyLog;
    private Date date;
    private Integer drinks;
    private String context;
    private String consequences;
    
    private GenericDAO logDAO;
    
    public KeepResultsController() {
        dailyLog = new DailyLog();
        try {
            this.daoBase = new GenericDAO<KeepResults>(KeepResults.class);
            logDAO = new GenericDAO<DailyLog>(DailyLog.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public KeepResults getKeepResults() {
        if(keepResults == null && loggedUser()){
            keepResults = getLoggedUser().getKeepResults();
            if(keepResults == null){
                keepResults = new KeepResults();
                keepResults.setUser(getLoggedUser());
            }
        }
        
        return keepResults;
    }
    
    public String intro(){
        if(getUser().isFemale()){
            return "mantendo-resultados-meta-mulher.xhtml?faces-redirect=true";
        }else{
            return "mantendo-resultados-meta-homem.xhtml?faces-redirect=true";
        }
    }
    
    public String goals(){ 
        try {
            daoBase.insertOrUpdate(keepResults, getEntityManager());
            return "mantendo-resultados-registro.xhtml?faces-redirect=true";
        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
   public void saveLog(){
       if(date == null){
           dailyLog.setDate(date);
           dailyLog.setKeepResults(keepResults);
       }
        
        try {
            logDAO.insertOrUpdate(dailyLog, getEntityManager());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro salvo com sucesso.", null));
        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void fetchLog(){
       dailyLog = new DailyLog();
       for(DailyLog log : getKeepResults().getDailyLogs()){
           if(log.getDate() == date){
               dailyLog = log;
           }
       }                
    }
    
    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
    
    public User getUser(){
            return getKeepResults().getUser();
    }

    public DailyLog getDailyLog() {
        return dailyLog;
    }

    public void setDailyLog(DailyLog dailyLog) {
        this.dailyLog = dailyLog;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getDrinks() {
        return drinks;
    }

    public void setDrinks(Integer drinks) {
        this.drinks = drinks;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getConsequences() {
        return consequences;
    }

    public void setConsequences(String consequences) {
        this.consequences = consequences;
    }

   
}
