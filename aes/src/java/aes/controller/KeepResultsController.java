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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
public class KeepResultsController extends BaseController<KeepResults> {

    private KeepResults keepResults;
    private DailyLog dailyLog;
    private Date date;

    private GenericDAO logDAO;

    public KeepResultsController() {
        try {
            daoBase = new GenericDAO<KeepResults>(KeepResults.class);
            logDAO = new GenericDAO<DailyLog>(DailyLog.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public KeepResults getKeepResults() {
        if (keepResults == null && loggedUser()) {
            keepResults = getLoggedUser().getKeepResults();
            if (keepResults == null) {
                keepResults = new KeepResults();
                keepResults.setUser(getLoggedUser());
            }
        }

        return keepResults;
    }

    public String intro() {
        if (getUser().isFemale()) {
            return "mantendo-resultados-meta-mulher.xhtml?faces-redirect=true";
        } else {
            return "mantendo-resultados-meta-homem.xhtml?faces-redirect=true";
        }
    }

    public String goals() {
        try {
            daoBase.insertOrUpdate(getKeepResults(), getEntityManager());
            return "mantendo-resultados-registro.xhtml?faces-redirect=true";

        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class
                    .getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public void saveLog() {
        if(dailyLog.getDate() == null){
            dailyLog.setDate(date);
            dailyLog.setKeepResults(getKeepResults());
        }
        try {
            logDAO.insertOrUpdate(dailyLog, getEntityManager());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro salvo com sucesso.", null));
        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void fetchLog() {
        dailyLog = new DailyLog();
        try {
            List<DailyLog> list = logDAO.list("keepResults", getKeepResults(), getEntityManager());
            for (DailyLog log : list) {
                if (log.getDate() != null && log.getDate().equals(date)) {
                    dailyLog = log;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public User getUser() {
        return getKeepResults().getUser();
    }
       
    public String[] getDates(){
        try {
            List<DailyLog> logs = logDAO.list("keepResults", getKeepResults(), getEntityManager());
            ArrayList<String> dates = new ArrayList<String>();         
            for(DailyLog log : logs){
                if(log.getDate() != null)
                dates.add(new SimpleDateFormat("yyyy-MM-dd").format(log.getDate()));
            }
            String[] datesArray = new String[dates.size()];
            datesArray = dates.toArray(datesArray);

            return datesArray;
        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }              
        
    }  

    public DailyLog getDailyLog() {
        if(dailyLog == null){
            dailyLog = new DailyLog();
        }
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

}
