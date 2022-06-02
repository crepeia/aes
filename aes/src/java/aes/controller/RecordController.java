package aes.controller;

import aes.model.DailyLog;
import aes.model.Evaluation;
import aes.model.Record;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.PDFGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named("recordController")
@SessionScoped
public class RecordController extends BaseController<Record> {

    private Record record;
    private DailyLog dailyLog;
    private Date date;
    private LocalDate localDate;
    
    private GenericDAO logDAO;

    @Inject
    private UserController userController;
    @Inject
    private ContactController contactController;

    private Evaluation lastEvaluation;

    @PostConstruct()
    public void init() {
        try {
            daoBase = new GenericDAO<Record>(Record.class);
            logDAO = new GenericDAO<DailyLog>(DailyLog.class);
            daoBase.setEntityManager(getEntityManager());
            logDAO.setEntityManager(getEntityManager());

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
                save();
            }
        }

        return record;
    }

    public User getUser() {
        return userController.getUser();
    }

    public void save() {
        try {
            daoBase.insertOrUpdate(getRecord());
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

    public void checkDailyGoal() {
        if ((getUser().isFemale() && getRecord().getDailyGoal() > 1) || (getUser().isMale() && getRecord().getDailyGoal() > 2)) {
            FacesContext.getCurrentInstance().addMessage("warn1", new FacesMessage(FacesMessage.SEVERITY_WARN, userController.getString("record.warning.dailyGoal"), null));
        }
    }

    public void checkWeeklyGoal() {
        if ((getUser().isFemale() && getRecord().getWeeklyGoal() > 5) || (getUser().isMale() && getRecord().getWeeklyGoal() > 10)) {
            FacesContext.getCurrentInstance().addMessage("warn2", new FacesMessage(FacesMessage.SEVERITY_WARN, userController.getString("record.warning.weeklyGoal"), null));
        }
    }

    public void saveLog() {
        try {
            logDAO.insertOrUpdate(dailyLog);
            FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, userController.getString("record.save"), null));
            if ((getUser().isFemale() && dailyLog.getDrinks() > 1) || (getUser().isMale() && dailyLog.getDrinks() > 2)) {
                FacesContext.getCurrentInstance().addMessage("warn1", new FacesMessage(FacesMessage.SEVERITY_WARN, userController.getString("record.warning.limit"), null));
            }
            if (dailyLog.getDrinks() > getRecord().getDailyGoal()) {
                FacesContext.getCurrentInstance().addMessage("warn2", new FacesMessage(FacesMessage.SEVERITY_WARN, userController.getString("record.warning.goal"), null));
            }
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void fetchLog() {
        try {
            dailyLog = null;
            List<DailyLog> list = logDAO.list("record", getRecord());
            for (DailyLog log : list) {
                if (log.getLogDate() != null && log.getLogDate().equals(localDate)) {
                    dailyLog = log;
                }
            }
            if (dailyLog == null) {
                dailyLog = new DailyLog();
                dailyLog.setLogDate(localDate);
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
            List<DailyLog> logs = logDAO.list("record", getRecord());
            dates = dates.concat("\"");
            if (!logs.isEmpty()) {
                for (DailyLog log : logs) {
                    if (log.getLogDate() != null) {
                        dateStr = log.getLogDate().toString();
                        dates = dates.concat(dateStr + ",");
                    }
                }
                dates = dates.substring(0, dates.length() - 1);
            }
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
        return dates;
    }

    public String getWeekFirstDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
    }

    public String getWeekLastDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
    }

    public void sendRecord() {
        contactController.sendRecordEmail(getUser(), "meuregistro.pdf", getRecordPDF());
        FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, userController.getString("record.send"), null));
    }

    public StreamedContent printRecord() {
        ByteArrayOutputStream pdf = getRecordPDF();
        return new DefaultStreamedContent(new ByteArrayInputStream(pdf.toByteArray()),
                "application/pdf", "meuregistro.pdf");
    }

    public ByteArrayOutputStream getRecordPDF() {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("aes/utility/record-template.html");
            byte[] buffer = new byte[102400];
            String template = new String(buffer, 0, input.read(buffer), StandardCharsets.US_ASCII);

            ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(getUser().getPreferedLanguage()));
            template = template.replace("#dailyGoal#", bundle.getString("daily.goal") + String.valueOf(getRecord().getDailyGoal()));
            template = template.replace("#weeklyGoal#", bundle.getString("weekly.goal") + String.valueOf(getRecord().getWeeklyGoal()));
            template = template.replace("#title#", bundle.getString("record0"));
            template = template.replace("#header1#", bundle.getString("estrategia.dim.registro.elet.day"));
            template = template.replace("#header2#", bundle.getString("estrategia.dim.registro.elet.doses"));
            template = template.replace("#header3#", bundle.getString("estrategia.dim.registro.elet.context"));
            template = template.replace("#header4#", bundle.getString("estrategia.dim.registro.elet.outcomes"));
            template = template.replace("#header5#", bundle.getString("estrategia.dim.registro.elet.daily"));

            List<DailyLog> logs = logDAO.listOrdered("record", getRecord(), "logDate");
            for (DailyLog log : logs) {
                template = template.replace("#row#",
                        "<tr>"
                        + "<td align='left'>"
                        + "<p  style='text-align:center;color:#999999;font-size:14px;font-weight:normal;line-height:19px;'>"
                        //+ new SimpleDateFormat("dd/MM/yyyy").format(log.getLogDate())
                        + log.getLogDate().toString()
                        + "</p>"
                        + "</td>"
                        + "<td align='left'>"
                        + "<p  style='text-align:center;color:#999999;font-size:14px;font-weight:normal;line-height:19px;'>"
                        + String.valueOf(log.getDrinks())
                        + "</p>"
                        + "</td>"
                        + "<td align='left'>"
                        + "<p  style='text-align:center;color:#999999;font-size:14px;font-weight:normal;line-height:19px;'>"
                        + log.getContext()
                        + "</p>"
                        + "</td>"
                        + "<td align='left'>"
                        + "<p  style='text-align:center;color:#999999;font-size:14px;font-weight:normal;line-height:19px;'>"
                        + log.getConsequences()
                        + "</p>"
                        + "</td>"
                        + "<td align='left'>"
                        + "<p  style='text-align:center;color:#999999;font-size:14px;font-weight:normal;line-height:19px;'>"
                        + (log.getDrinks() >= getRecord().getDailyGoal() ? bundle.getString("no") : bundle.getString("yes"))
                        + "</p>"
                        + "</td>"
                        + "</tr>"
                        + "#row#");
            }
            template = template.replace("#row#", " ");

            PDFGenerator pdfGenerator = new PDFGenerator();
            return pdfGenerator.generatePDF(template);
        } catch (IOException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getURL() {
        String url = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("url");
        if (url == null) {
            return "index.xhtml?faces-redirect=true";
        }
        return url;
    }

    public DailyLog getDailyLog() {
        if (dailyLog == null) {
            dailyLog = new DailyLog();
        }
        return dailyLog;
    }

    public Evaluation getLatestEvaluation() {

        try {
            GenericDAO daoEvaluation = new GenericDAO<Evaluation>(Evaluation.class);
            if (userController.isLoggedIn()) {
                List<Evaluation> evaluations = daoEvaluation.listOrdered("user", getUser(), "dateCreated");
                if (!evaluations.isEmpty()) {
                    lastEvaluation = evaluations.get(evaluations.size() - 1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (NamingException ex) {
            Logger.getLogger(RecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastEvaluation;
    }

    public void setLastEvaluation(Evaluation lastEvaluation) {
        this.lastEvaluation = lastEvaluation;
    }

    public void setDailyLog(DailyLog dailyLog) {
        this.dailyLog = dailyLog;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        this.localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public ContactController getContactController() {
        return contactController;
    }

    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

}
