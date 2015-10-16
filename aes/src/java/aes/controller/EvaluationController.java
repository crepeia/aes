/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Contact;
import aes.model.Evaluation;
import aes.model.Plan;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.EMailSSL;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author thiagorizuti and danielapereira
 */
@ManagedBean(name = "evaluationController")
@SessionScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int HOURS_LIMIT = 24 * 7;

    private Evaluation evaluation;

    private Integer day;
    private Integer month;
    private Integer year;
    
    private String url;

    public EvaluationController() {
        try {
            this.daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Evaluation getEvaluation() {
        if (evaluation == null) {
            GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
            gc.add(GregorianCalendar.HOUR, EvaluationController.HOURS_LIMIT);

            if (loggedUser()) {
                try {
                    List<Evaluation> evaluations = this.getDaoBase().list("user", getLoggedUser(), this.getEntityManager());
                    for (Evaluation e : evaluations) {
                        if (gc.after(e.getDate())) {
                            evaluation = e;
                        }
                    }
                    if (evaluation == null) {
                        evaluation = new Evaluation();
                        evaluation.setDate(Calendar.getInstance());
                        evaluation.setUser(getLoggedUser());
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                User user = new User();
                evaluation = new Evaluation();
                evaluation.setDate(Calendar.getInstance());
                evaluation.setUser(user);
            }

        }

        return evaluation;
    }

    public User getUser() {
        return getEvaluation().getUser();
    }

    public String intro() {
        try {
            getUser().setBirth(year, month, day);
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            if (getUser().getPregnant() && !getEvaluation().getDrink()) {
                return "quanto-voce-bebe-nao-gravidez.xhtml?faces-redirect=true";
            } else if (getUser().getPregnant() && getEvaluation().getDrink()) {
                return "quanto-voce-bebe-sim-gravidez.xhtml?faces-redirect=true";
            } else if (getUser().isUnderage() && !getEvaluation().getDrink()) {
                return "quanto-voce-bebe-nao-adoles.xhtml?faces-redirect=true";
            } else if (getUser().isUnderage() && getEvaluation().getDrink()) {
                return "quanto-voce-bebe-sim-adoles.xhtml?faces-redirect=true";
            } else if (!getEvaluation().getDrink()) {
                return "quanto-voce-bebe-abstemio.xhtml?faces-redirect=true";
            } else if (loggedUser()) {
                return "quanto-voce-bebe-sim-beber-uso-audit-3.xhtml?faces-redirect=true";
            } else {
                return "quanto-voce-bebe-convite.xhtml?faces-redirect=true";
            }
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }

    }

    public void yearEmail() {
        try {
            getEvaluation().setYearEmail(true);
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Você será contactado em breve."));
            ((InputText) getComponent("email")).setDisabled(true);
            ((CommandButton) getComponent("sendButton")).setDisabled(true);
            if (!loggedUser()) {
                FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            }
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String audit3() {
        try {
            this.getDaoBase().insertOrUpdate(getEvaluation(), this.getEntityManager());

            int age = getUser().getAge();
            int drinkingnDays = getEvaluation().getDrinkingDays();
            int weekTotal = getEvaluation().getWeekTotal();
            int audit3sum = getEvaluation().getAudit3Sum();

            if (audit3sum > 6 || (getUser().isFemale() && drinkingnDays > 1) || (getUser().isMale() && drinkingnDays > 2) || (getUser().isFemale() && weekTotal > 5) || (getUser().isMale() && weekTotal > 10)) {
                return "quanto-voce-bebe-sim-beber-uso-audit-7.xhtml?faces-redirect=true";
            } else if (audit3sum <= 6 && (((getUser().isFemale() && drinkingnDays <= 1) || (getUser().isMale() && drinkingnDays <= 2)) && ((getUser().isFemale() && weekTotal <= 5) || (getUser().isMale() && weekTotal <= 10)))) {
                if (getUser().isMale() && age <= 65) {
                    return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
                } else if ((getUser().isMale() && age > 65) || getUser().isFemale()) {
                    return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String audit7() {
        try {
            this.getDaoBase().insertOrUpdate(getEvaluation(), this.getEntityManager());

            int age = getUser().getAge();
            int drinkingDays = getEvaluation().getDrinkingDays();
            int weekTotal = getEvaluation().getWeekTotal();
            int auditFull = getEvaluation().getAuditFullSum();

            if ((auditFull <= 17) && ((getUser().isFemale() && drinkingDays <= 1) || (getUser().isMale() && drinkingDays <= 2)) && ((getUser().isFemale() && weekTotal <= 5) || (getUser().isMale() && weekTotal <= 10))) {
                if (getUser().isFemale() && age <= 65) {
                    return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
                } else if ((getUser().isMale() && age > 65) || getUser().isFemale()) {
                    return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
                }
            } else if ((auditFull <= 17) && ((getUser().isFemale() && drinkingDays > 1) || (getUser().isMale() && drinkingDays > 2)) && ((getUser().isFemale() && weekTotal > 5) || (getUser().isMale() && weekTotal > 10))) {
                return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-baixo-risco-limites?faces-redirect=true";
            } else if (auditFull >= 18 && auditFull <= 25) {
                return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-risco.xhtml?faces-redirect=true";
            } else if (auditFull >= 26 && auditFull <= 29) {
                return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-nocivo.xhtml?faces-redirect=true";
            } else if (auditFull >= 30 && auditFull <= 50) {
                return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-dependencia.xhtml?faces-redirect=true";
            }
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";

    }

    public boolean q4() {
        try {
            return getEvaluation().getAudit4() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public boolean q5() {
        try {
            return getEvaluation().getAudit5() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public boolean q6() {
        try {
            return getEvaluation().getAudit6() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public boolean q7() {
        try {
            return getEvaluation().getAudit7() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public boolean q8() {
        try {
            return getEvaluation().getAudit8() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public boolean q9() {
        try {
            return getEvaluation().getAudit9() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public boolean q10() {
        try {
            return getEvaluation().getAudit10() != 0;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public String symptoms() {
        setURL();
        return "preparando-pros-cons.xhtml?faces-redirect=true";
    }

    public String prosCons() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            return "preparando-pros-cons-avaliacao.xhtml?faces-redirect=true";
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public String prosConsEvaluation() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            if (getEvaluation().getReady()) {
                setURL();
                return "preparando-diminuir-ou-parar.xhtml?faces-redirect=true";
            } else {
                return "preparando-diminuir-parar-nao.xhtml?faces-redirect=true";

            }

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String cutDownQuitNo() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            if (getEvaluation().getBackPlan()) {
                setURL();
                return "preparando-diminuir-ou-parar.xhtml?faces-redirect=true";
            } else {
                getEvaluation().setYearEmail(true);
                daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
                ((CommandButton) getComponent("saveBtn")).setDisabled(true);
                FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Você será contactado dentro de um ano."));
            }

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String cutDownQuit() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            if (getEvaluation().getQuit()) {
                return "estrategia-diminuir-introducao.xhtml?faces-redirect=true";
            } else {
                setURL();
                return "estrategia-parar-apoio-intro.xhtml?faces-redirect=true";
            }

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public void dependenceListener(){
            try {
                daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            } catch (SQLException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public String dependenceNext(){     
        try {
            setURL();
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            return ("estrategia-parar-apoio-intro.xhtml?faces-redirect=true");

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public String nextRegEleManAndWoman() {
        try {
            daoBase.insertOrUpdate(this.getEvaluation(), this.getEntityManager());
            setURL();
            return "estrategia-diminuir-registro-eletronico.xhtml?faces-redirect=true";
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public String goToPlan(){
        setURL();
        return "plano-mudanca.xhtml?faces-redirect=true";
    }
       
    public String printPlan(){
       try {
            daoBase.insertOrUpdate(this.getEvaluation(), this.getEntityManager());
            return "plano-mudanca-pronto-para-comecar-print.xhtml?faces-redirect=true";
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        } 
       return "";
    }
    
    public void sendPlan(){
        try {
            Contact contact = new Contact();
            contact.setUser(getUser());
            contact.setRecipient("t.rizuti@gmail.com");
            contact.setSender("alcoolesaude@gmail.com");
            contact.setSentDate(Calendar.getInstance());
            contact.setSubject("Plano");
            contact.setTextContent("Seu plano em anexo");
            contact.setAttachmentName("meuplano.pdf");
            contact.setAttachment(new Plan(getEvaluation()).getPdf());
            contact.setHTMLTemplate("/resources/default/templates/plan-template.html",
                    "Alcool e Saude", "Plano", "Em Anexo seu plano");
            EMailSSL eMailSSL = new EMailSSL();
            eMailSSL.send(contact);
            new GenericDAO(Contact.class).insertOrUpdate(contact, this.getEntityManager());
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Plano enviado."));
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public StreamedContent getPlanPdf() {   
            Plan plan = new Plan(getEvaluation());
            return new DefaultStreamedContent(new ByteArrayInputStream(plan.getPdf().toByteArray()), 
                    "application/pdf", "meuplano.pdf");
    }
    
    public String getURL() {
        if (url == null) {
            return "index.xhtml?faces-redirect=true";
        }
        return url;
    }
    
    public void setURL() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.lastIndexOf('/') + 1);
    }
    
    public Date getCurrentDate(){
        return new Date();
    }


    public Integer getDay() {
        if (loggedUser()) {
            return this.getUser().getBirth().get(Calendar.DAY_OF_MONTH);
        }
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        if (loggedUser()) {
            return this.getUser().getBirth().get(Calendar.MONTH);
        }
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        if (loggedUser()) {
            return this.getUser().getBirth().get(Calendar.YEAR);
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String teste(){
        return "existem campos em branco";
    }

}
