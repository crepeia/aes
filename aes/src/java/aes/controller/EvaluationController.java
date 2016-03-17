/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Evaluation;
import aes.model.Plan;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.PDFGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javax.servlet.ServletContext;
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

    private String planTemplate;

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
        if (evaluation == null) {
            if (userController.isLoggedIn()) {
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

    public String nextRegEleManAndWoman() {
        try {
            daoBase.insertOrUpdate(this.getEvaluation(), this.getEntityManager());
            saveURL();
            return "estrategia-diminuir-registro-eletronico.xhtml?faces-redirect=true";

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String regEl() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), getEntityManager());
            return "estrategia-diminuir-alternativas.xhtml?faces-redirect=true";

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String goToPlan() {
        saveURL();
        return "plano-mudanca.xhtml?faces-redirect=true";
    }

    public String printPlan() {
        try {
            daoBase.insertOrUpdate(this.getEvaluation(), this.getEntityManager());
            return "plano-mudanca-pronto-para-comecar-print.xhtml?faces-redirect=true";

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public ByteArrayOutputStream createPlan() {
        String content[] = new String[6];
        content[0] = new SimpleDateFormat("dd/MM/yyyy").format(getEvaluation().getDataComecarPlano());
        content[1] = getEvaluation().getRazoesPlano();
        content[2] = getEvaluation().getEstrategiasPlano();
        content[3] = getEvaluation().getPessoasPlano();
        content[4] = getEvaluation().getSinaisSucessoPlano();
        content[5] = getEvaluation().getPossiveisDificuladesPlano();
        String plan = fillPlanTemplate(content);
        return new PDFGenerator().generatePDF(plan);
    }

    /* public void sendPlan() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Contact contact = new Contact();
        contact.setRecipient(getUser().getEmail());
        contact.setSender("alcoolesaude@gmail.com");
        contact.setDateSent(new Date());
        contact.setSubject("Álcool e Saúde - Seu Plano");
        contact.setText("Em anexo está seu plano em formato PDF.");
        contact.setPdfName("meuplano.pdf");
        contact.setPdf(new Plan(getEvaluation()).getPdf());
        contact.setHtml(fillContactTemplate("Álcool e Saúde", "Seu Plano Personalizado", "Em anexo está seu plano em formato PDF."));
        getContactController().sendEmail(contact);
        FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Plano enviado."));

    }*/
    public String fillPlanTemplate(String content[]) {
        try {
            if (planTemplate == null) {
                planTemplate = readTemplate("/resources/default/templates/plan-template.html");

            }
        } catch (IOException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        String template = planTemplate;
        String title = "Meu Plano";
        String subtitle[] = new String[6];
        subtitle[0] = "Data para começar";
        subtitle[1] = "As razões mais importantes que eu tenho para mudar a forma que bebo são:";
        subtitle[2] = "Eu irei usar as seguintes estratégias:";
        subtitle[3] = "As pessoas que podem me ajudar são:";
        subtitle[4] = "Eu saberei que meu plano está funcionando quando:";
        subtitle[5] = "O que pode interferir e como posso lidar com estas situações:";
        if (planTemplate == null) {
            System.out.println("null1");
        }
        if (template == null) {
            System.out.println("null2");
        }
        template = template.replace("#title#", title);
        for (int i = 0; i < 6; i++) {
            if (content[i] != null && !content[i].trim().isEmpty()) {
                template = template.replace("#subtitle" + (i + 1) + "#", subtitle[i]);
                template = template.replace("#content" + (i + 1) + "#", content[i]);
            } else {
                template = template.replace("#subtitle" + (i + 1) + "#", "");
                template = template.replace("#content" + (i + 1) + "#", "");
            }
        }
        return template;
    }

    public String readTemplate(String path) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        String absolutePath = servletContext.getRealPath(path);
        byte[] encoded = Files.readAllBytes(Paths.get(absolutePath));
        String template = new String(encoded, StandardCharsets.UTF_8);
        return template;
    }

    public StreamedContent getPlanPdf() {
        Plan plan = new Plan(getEvaluation());
        return new DefaultStreamedContent(new ByteArrayInputStream(plan.getPdf().toByteArray()),
                "application/pdf", "meuplano.pdf");
    }

    public String getEndingDay() {
        if (getEvaluation().getStartingDay() == null) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(getEvaluation().getStartingDay());
        cal.add(Calendar.DATE, 6);
        return new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
    }

    /*public String getDayName(int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getEvaluation().getStartingDay());
        cal.add(Calendar.DATE, day - 1);
        return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, getLanguageController().getLocale());
    }*/
    public String getDay(int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getEvaluation().getStartingDay());
        cal.add(Calendar.DATE, day - 1);
        return new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
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

}
