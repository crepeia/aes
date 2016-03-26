package aes.controller;

import aes.model.DailyLog;
import aes.model.Record;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    public void init() {
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

    public User getUser() {
        return userController.getUser();
    }

    public void save() {
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

    public void saveLog() {
        try {
            logDAO.insertOrUpdate(dailyLog, getEntityManager());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro salvo com sucesso.", null));
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void fetchLog() {
        try {
            dailyLog = null;
            List<DailyLog> list = logDAO.list("record", getRecord(), getEntityManager());
            for (DailyLog log : list) {
                if (log.getLogDate() != null && log.getLogDate().equals(date)) {
                    dailyLog = log;
                }
            }
            if (dailyLog == null) {
                dailyLog = new DailyLog();
                dailyLog.setLogDate(date);
                dailyLog.setRecord(getRecord());
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

    public String getDates() {
        String dates = new String();
        String dateStr;
        try {
            List<DailyLog> logs = logDAO.list("record", getRecord(), getEntityManager());
            dates = dates.concat("\"");
            for (DailyLog log : logs) {
                if (log.getLogDate() != null) {
                    dateStr = new SimpleDateFormat("yyyy-MM-dd").format(log.getLogDate());
                    dates = dates.concat(dateStr + ",");
                }
            }
            dates = dates.substring(0, dates.length() - 1);
            dates = dates.concat("\"");
            return dates;
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
            return dates;
        }

    }

    public String getWeek() {
        String dates = new String();    
        String dateStr;
        Calendar cal;
        dates = dates.concat("\"");
        for (int i = 1; i <= 7; i++) {
           cal = Calendar.getInstance();
           cal.add(Calendar.DATE, -i); 
           dateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
           dates = dates.concat(dateStr + ",");
        }
        dates = dates.substring(0, dates.length() - 1);
        dates = dates.concat("\"");
        System.out.println("getWeek()");
        System.out.println(dates);
        return dates;
    }
    
    public String getWeekFirstDay(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
    }
    
    public String getWeekLastDay(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
    }
    
    public DailyLog getDailyLog() {
        if (dailyLog == null) {
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
