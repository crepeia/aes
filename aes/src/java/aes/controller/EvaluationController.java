/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.PDFGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
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
import javax.servlet.http.HttpServletRequest;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author thiagorizuti and danielapereira
 */
@ManagedBean(name = "evaluationController")
@SessionScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int DAYS_LIMIT = -7;

    private Evaluation evaluation;

    private String htmlTemplate;

    @ManagedProperty(value = "#{userController}")
    private UserController userController;
    @ManagedProperty(value = "#{contactController}")
    private ContactController contactController;

    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Evaluation getEvaluation() {
        if (evaluation == null && userController.isLoggedIn()) {
            try {
                List<Evaluation> evaluations = this.getDaoBase().list("user", getUser(), this.getEntityManager());
                if (!evaluations.isEmpty()) {
                    Calendar limit = Calendar.getInstance();
                    limit.add(Calendar.DATE, EvaluationController.DAYS_LIMIT);
                    Calendar evaluationDate = Calendar.getInstance();
                    for (Evaluation e : evaluations) {
                        evaluationDate.setTime(e.getDateCreated());
                        if (evaluationDate.after(limit)) {
                            evaluation = e;
                        }
                    }
                } else {
                    evaluation = new Evaluation();
                    evaluation.setDateCreated(new Date());
                    evaluation.setUser(getUser());
                }

            } catch (SQLException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return evaluation;
    }

    public User getUser() {
        return userController.getUser();
    }

    public void save() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String audit3() {
        int age = getUser().getAge();
        int drinkingDays = getEvaluation().getDrinkingDays();
        int weekTotal = getEvaluation().getWeekTotal();
        int audit3sum = getEvaluation().getAudit3Sum();
        if (audit3sum > 6 || (getUser().isFemale() && drinkingDays > 1) || (getUser().isMale() && drinkingDays > 2) || (getUser().isFemale() && weekTotal > 5) || (getUser().isMale() && weekTotal > 10)) {
            return "quanto-voce-bebe-sim-beber-uso-audit-7.xhtml?faces-redirect=true";
        } else if (audit3sum <= 6 && (((getUser().isFemale() && drinkingDays <= 1) || (getUser().isMale() && drinkingDays <= 2)) && ((getUser().isFemale() && weekTotal <= 5) || (getUser().isMale() && weekTotal <= 10)))) {
            if (getUser().isMale() && age <= 65) {
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
            } else if ((getUser().isMale() && age > 65) || getUser().isFemale()) {
                return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
            }
        }
        return "";

    }

    public String audit7() {
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
        return "";
    }

    public String readyToChange() {
        if (getEvaluation().getReady()) {
            saveURL();
            return "preparando-diminuir-ou-parar.xhtml?faces-redirect=true";
        } else {
            return "preparando-diminuir-parar-nao.xhtml?faces-redirect=true";

        }
    }

    public String goBack() {
        if (getEvaluation().getGoBack()) {
            saveURL();
            return "preparando-diminuir-ou-parar.xhtml?faces-redirect=true";
        } else {
            ((CommandButton) getComponent("saveBtn")).setDisabled(true);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Você será contactado dentro de um ano.", null));
            return "";
        }
    }

    public String dependenceContinue() {
        getEvaluation().setQuit(true);
        saveURL();
        return ("estrategia-parar-apoio-intro.xhtml?faces-redirect=true");
    }

    public String cutDownQuit() {
        if (getEvaluation().getQuit()) {
            return "estrategia-diminuir-introducao.xhtml?faces-redirect=true";
        } else {
            saveURL();
            return "estrategia-parar-apoio-intro.xhtml?faces-redirect=true";

        }
    }

    public String goToPlan() {
        saveURL();
        return "plano-mudanca.xhtml?faces-redirect=true";
    }

    public void sendPlan() {
        contactController.sendPlanEmail(getUser(), getEvaluation().getPlanContent());
        FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Plano enviado."));
    }

    public StreamedContent getPlanPdf() {
        ByteArrayOutputStream pdf = (new PDFGenerator()).generatePDF(contactController.fillHTMLTemplate(getEvaluation().getPlanContent()));
        return new DefaultStreamedContent(new ByteArrayInputStream(pdf.toByteArray()),
                "application/pdf", "meuplano.pdf");
    }

    public String getURL() {
        String url = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("url");
        if (url == null) {
            return "index.xhtml?faces-redirect=true";
        }
        return url;
    }

    public void saveURL() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.lastIndexOf('/') + 1);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("url", url);
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public ContactController getContactController() {
        return contactController;
    }

    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

}
