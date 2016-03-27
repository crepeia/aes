package aes.controller;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.PDFGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
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
import org.primefaces.component.inputtext.InputText;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean(name = "evaluationController")
@SessionScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int DAYS_LIMIT = -7;

    private Evaluation evaluation;

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
                }
                if (evaluation == null) {
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
            annualScreening();
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

    public void annualScreening() {
        contactController.clearScheduledEmails(getUserController().getUser());
        contactController.scheduleAnnualScreeningEmail(getUserController().getUser());
        ((CommandButton) getComponent("saveBtn")).setDisabled(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Parabéns por completar sua avaliação. Entraremos em contato dentro de um ano.", null));
        if (!userController.isLoggedIn()) {
            ((InputText) getComponent("email")).setDisabled(true);
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        }
    }

    public void savePlan() {
        save();
        contactController.clearScheduledEmails(getUser());
        contactController.scheduleWeeklyEmail(getUser());
        FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, "Parabéns por completar sua avaliação. Você pode imprimir seu plano ou podemos enviá-lo via email.", null));
    }

    public void sendPlan() {
        contactController.sendPlanEmail(getUser(), "meuplano.pdf", getPlanPDF());
        FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, "Plano enviado.", null));
    }

    public StreamedContent printPlan() {
        ByteArrayOutputStream pdf = getPlanPDF();
        return new DefaultStreamedContent(new ByteArrayInputStream(pdf.toByteArray()),
                "application/pdf", "meuplano.pdf");
    }

    public ByteArrayOutputStream getPlanPDF() {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("aes/utility/plan-template.html");
            byte[] buffer = new byte[102400];
            String template = new String(buffer, 0, input.read(buffer), StandardCharsets.UTF_8);

            ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(getUser().getPreferedLanguage()));
            String subtitle[] = new String[6];
            String title = bundle.getString("plan0");
            subtitle[0] = bundle.getString("plan1");
            subtitle[1] = bundle.getString("plan2");
            subtitle[2] = bundle.getString("plan3");
            subtitle[3] = bundle.getString("plan4");
            subtitle[4] = bundle.getString("plan5");
            subtitle[5] = bundle.getString("plan6");
            template = template.replace("#title#", title);
            for (int i = 0; i < 6; i++) {
                template = template.replace("#subtitle" + (i + 1) + "#", subtitle[i]);
                template = template.replace("#content" + (i + 1) + "#", getEvaluation().getPlanContent()[i]);
            }

            PDFGenerator pdfGenerator = new PDFGenerator();
            return pdfGenerator.generatePDF(template);
        } catch (IOException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
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

    public void saveURL() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.lastIndexOf('/') + 1);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("url", url);
    }

    public Date getCurrentDate() {
        return new Date();
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
