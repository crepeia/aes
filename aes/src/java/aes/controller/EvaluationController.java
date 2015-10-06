/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
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
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private boolean continueEvaluation;

    private StreamedContent planoPersonalizado;

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
    
    public String nextEstrategia(){
        setURL();
        return "plano-mudanca.xhtml?faces-redirect=true";
    }
    
     public void estrategiaRegistro() {
        if (getUser().getGender() == 'F') {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-diminuir-registro-eletronico-meta-mulher.xhtml?faces-redirect=true");
            } catch (IOException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-diminuir-registro-eletronico-meta-homem.xhtml?faces-redirect=true");
            } catch (IOException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String nextRegEleManAndWoman() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        setURL();
        return "estrategia-diminuir-registro-eletronico.xhtml?faces-redirect=true";
    }

    public ByteArrayOutputStream gerarPdf() {

        try {
            this.getDaoBase().insertOrUpdate(getEvaluation(), this.getEntityManager());
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, os);
            document.open();

            document.addTitle("Plano Personalizado");
            document.addAuthor("aes.com.br");

            URL url = FacesContext.getCurrentInstance().getExternalContext().getResource("/resources/default/images/logo-alcool-e-saude.png");
            Image img = Image.getInstance(url);
            img.setAlignment(Element.ALIGN_CENTER);
            img.scaleToFit(75, 75);
            document.add(img);

            Color color = Color.getHSBColor(214, 81, 46);
            Font f1 = new Font(FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
            f1.setColor(22, 63, 117);
            Font f2 = new Font(FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLUE);
            f2.setColor(22, 63, 117);
            Font f3 = new Font(FontFamily.HELVETICA, 11);

            Paragraph paragraph = new Paragraph("Meu plano", f1);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            /* paragraph = new Paragraph(this.getText("pronto.plano.padrao.h2.1"), f2);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p1"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p2"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p3"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p4"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p5"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p5.1"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p5.2"), f3);
             document.add(paragraph);
             paragraph = new Paragraph(this.getText("dicas.p6"), f3);
             document.add(paragraph);
             document.add(Chunk.NEWLINE);*/
            if (getEvaluation().getDataComecarPlano() != null && !getEvaluation().getRazoesPlano().trim().isEmpty()) {
                paragraph = new Paragraph("Data para começar", f2);
                document.add(paragraph);
                paragraph = new Paragraph(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getEvaluation().getDataComecarPlano()), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph.add(new Paragraph("teste1"));
            }

            if (getEvaluation().getRazoesPlano() != null && !getEvaluation().getRazoesPlano().trim().isEmpty()) {
                paragraph = new Paragraph(" As razões mais importantes que eu tenho para mudar a forma que bebo são: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getRazoesPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph = new Paragraph(" ");
                paragraph.add(paragraph);
            }

            if (getEvaluation().getEstrategiasPlano() != null && !getEvaluation().getEstrategiasPlano().trim().isEmpty()) {
                paragraph = new Paragraph("Eu irei usar as seguintes estratégias: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getEstrategiasPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph.add(new Paragraph(" "));
            }

            if (getEvaluation().getPessoasPlano() != null && !getEvaluation().getPessoasPlano().trim().isEmpty()) {
                paragraph = new Paragraph("As pessoas que podem me ajudar são: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getPessoasPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph.add(new Paragraph(" "));
            }

            if (getEvaluation().getSinaisSucessoPlano() != null || getEvaluation().getSinaisSucessoPlano().trim().isEmpty()) {
                paragraph = new Paragraph("Eu saberei que meu plano está funcionando quando: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getSinaisSucessoPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph.add(new Paragraph(" "));
            }

            if (getEvaluation().getPossiveisDificuladesPlano() != null || getEvaluation().getPossiveisDificuladesPlano().trim().isEmpty()) {
                paragraph = new Paragraph("O que pode interferir e como posso lidar com estas situações: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getPossiveisDificuladesPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph.add(new Paragraph(" "));
            }
            document.close();

            return os;
        } catch (Exception ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar dados", null));
            return null;
        }

    }

    public StreamedContent getPlanoPersonalizado() {

        InputStream is;
        try {
            is = new ByteArrayInputStream(gerarPdf().toByteArray());
            return new DefaultStreamedContent(is, "application/pdf", "plano.pdf");
        } catch (Exception e) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, "erro ao gerar PDF");
            return null;
        }

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

    public boolean isContinueEvaluation() {
        return continueEvaluation;
    }

    public void setContinueEvaluation(boolean continueEvaluation) {
        this.continueEvaluation = continueEvaluation;
    }

    public void setURL() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.lastIndexOf('/') + 1);
    }

    public String getURL() {
        if (url == null) {
            return "index.xhtml?faces-redirect=true";
        }
        return url;
    }

}
