/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.DailyLog;
import aes.model.Record;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiago
 */
@ManagedBean(name = "recordController")
@SessionScoped
public class RecordController extends BaseController<Record> {

    private Record record;
    private DailyLog dailyLog;
    private Date date;

    private GenericDAO logDAO;
    
    @ManagedProperty(value = "#{userController}")
    private UserController userController;
    
    @PostConstruct()
    public void init(){
        try {
            daoBase = new GenericDAO<Record>(Record.class);
            logDAO = new GenericDAO<DailyLog>(DailyLog.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Record getRecord() {
        if (record == null && userController.isLoggedIn()) {
            record = getUser().getRecord();
            if (record == null) {
                record = new Record();
                record.setUser(getUser());
            }
        }

        return record;
    }
    
    public User getUser(){
        return userController.getUser();
    }
    
    public void save(){
        try {
            daoBase.insertOrUpdate(getRecord(), getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            daoBase.insertOrUpdate(getRecord(), getEntityManager());
            return "mantendo-resultados-registro.xhtml?faces-redirect=true";

        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class
                    .getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public void saveLog() {
        if(dailyLog.getDate() == null){
            dailyLog.setDate(date);
            dailyLog.setRecord(getRecord());
        }
        try {
            logDAO.insertOrUpdate(dailyLog, getEntityManager());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro salvo com sucesso.", null));
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void fetchLog() {
        dailyLog = new DailyLog();
        try {
            List<DailyLog> list = logDAO.list("record", getRecord(), getEntityManager());
            for (DailyLog log : list) {
                if (log.getDate() != null && log.getDate().equals(date)) {
                    dailyLog = log;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
       
    public String[] getDates(){
        try {
            List<DailyLog> logs = logDAO.list("record", getRecord(), getEntityManager());
            ArrayList<String> dates = new ArrayList<String>();         
            for(DailyLog log : logs){
                if(log.getDate() != null)
                dates.add(new SimpleDateFormat("yyyy-MM-dd").format(log.getDate()));
            }
            String[] datesArray = new String[dates.size()];
            datesArray = dates.toArray(datesArray);

            return datesArray;
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
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

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }
    

}
