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
import static java.sql.Types.NULL;
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

    private User user;
    private Evaluation evaluation;

    private Integer gender;
    private Integer drink;
    private Integer day;
    private Integer month;
    private Integer year;
    private Integer pregnant;
    
    private int consumoDias;
    private int consumoDoses;
    
    private boolean continueEvaluation;
    
    private GenericDAO userDAO;
    
    private StreamedContent planoPersonalizado;

    public EvaluationController() {
        gender = 3;
        pregnant = 3;
        consumoDias = 0;
        consumoDoses = 0;
        try {
            this.userDAO = new GenericDAO(User.class);
            this.daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            String message = "Ocorreu um erro inesperado.";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }
    
    public boolean q4(){
        try{
            return evaluation.getAudit4()!= 0;
        }catch(NullPointerException e){
            return false;
        }
                
    }
    
    public boolean q5(){
        try{
            return evaluation.getAudit5()!= 0;
        }catch(NullPointerException e){
            return false;
        }
                
    }
    
    public boolean q6(){
        try{
            return evaluation.getAudit6()!= 0;
        }catch(NullPointerException e){
            return false;
        }
                
    }
    
    public boolean q7(){
        try{
            return evaluation.getAudit7()!= 0;
        }catch(NullPointerException e){
            return false;
        }
                
    }
    
    public boolean q8(){
        try{
            return evaluation.getAudit8()!= 0;
        }catch(NullPointerException e){
            return false;
        }
                
    }
    
    public boolean q9(){
        try{
            return evaluation.getAudit9()!= 0;
        }catch(NullPointerException e){
            return false;
        }
                
    }
    
    public boolean q10(){
        try{
            return evaluation.getAudit10()!= 0;
        }catch(NullPointerException e){
            return false;
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
                    if(evaluation == null){
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

    public User getUser(){
        return getEvaluation().getUser();
    }
    
    public String intro(){
        getUser().setBirth(year, month, day); 
        try{   
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager()); 
        }catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(getUser().getPregnant() && !getEvaluation().getDrink()){
            return "quanto-voce-bebe-nao-gravidez.xhtml";
        }else if(getUser().getPregnant() && getEvaluation().getDrink()){
            return "quanto-voce-bebe-sim-gravidez.xhtml";
        }else if(getUser().isUnderage() && !getEvaluation().getDrink()){
            return "quanto-voce-bebe-nao-adoles.xhtml";
        }else if(getUser().isUnderage() && getEvaluation().getDrink()){
            return "quanto-voce-bebe-sim-adoles.xhtml";
        }else if(!getEvaluation().getDrink()){
            return "quanto-voce-bebe-abstemio.xhtml";
        }else{
            return "quanto-voce-bebe-convite.xhtml";
        }
    }
    public void yearEmail() {
        getEvaluation().setYearEmail(true);
        try { 
            daoBase.insertOrUpdate(getEvaluation(), this.getEntityManager());
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Você será contactado em breve."));
            ((InputText) getComponent("email")).setDisabled(true);
            ((CommandButton) getComponent("sendButton")).setDisabled(true);
            if(!loggedUser())
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getConsumoDiasDoses(){
        int cont = 0;
        if(evaluation.getSunday() != 0)
            cont++;
        if(evaluation.getTuesday()!= 0)
            cont++;
        if(evaluation.getWednesday()!= 0)
            cont++;
        if(evaluation.getThursday()!= 0)
            cont++;
        if(evaluation.getFriday()!= 0)
            cont++;
        if(evaluation.getSaturday()!= 0)
            cont++;
        if(evaluation.getMonday()!= 0)
            cont++;
        consumoDias = cont;
        consumoDoses = evaluation.getSunday() + evaluation.getTuesday() + evaluation.getWednesday() + evaluation.getThursday() +  evaluation.getFriday() + evaluation.getSaturday() + evaluation.getMonday();
    }
    
   
    public void audit3() throws IOException{ 
        User userLocal = getUser();
        int age = userLocal.getAge();
        this.getConsumoDiasDoses();
        
        int sum1 = evaluation.getAudit1();
        int sum2 = evaluation.getAudit2();
        int sum3 = evaluation.getAudit3();
        int weekTotal = consumoDoses;
        int sumTotal = sum1 + sum2 + sum3;
        
        if(sumTotal > 6 || (this.getUser().getGender()=='F' && consumoDias > 1) || (this.getUser().getGender()=='M' && consumoDias > 2) || (this.getUser().getGender()=='F' && weekTotal > 5) || (this.getUser().getGender()=='M' && weekTotal > 10)){
            FacesContext.getCurrentInstance().getExternalContext().redirect("quanto-voce-bebe-sim-beber-uso-audit-7.xhtml");
        }else if(sumTotal <= 6 && (((this.getUser().getGender()=='F' && consumoDias <= 1) || (this.getUser().getGender()=='M' && consumoDias <= 2)) && ((this.getUser().getGender()=='F' && weekTotal <= 5) || (this.getUser().getGender()=='M' && weekTotal <= 10))) ){
            if(this.getUser().getGender() == 'M' && age <= 65)
                FacesContext.getCurrentInstance().getExternalContext().redirect("quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml");
            else if((this.getUser().getGender() == 'M' && age > 65) || this.getUser().getGender() == 'F')
                FacesContext.getCurrentInstance().getExternalContext().redirect("quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml");
        }
        try {
            this.getDaoBase().insertOrUpdate(evaluation, this.getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problema ao gravar dados", null));
        }
    }
    
    
    public String audit7(){
        
        int sum1 = evaluation.getAudit1();
        int sum2 = evaluation.getAudit2();
        int sum3 = evaluation.getAudit3();
        
        int sum4 = evaluation.getAudit4();
        int sum5 = evaluation.getAudit5();
        int sum6 = evaluation.getAudit6();
        int sum7 = evaluation.getAudit7();
        int sum8 = evaluation.getAudit8();
        int sum9 = evaluation.getAudit9();
        int sum10 = evaluation.getAudit10();
        this.getConsumoDiasDoses();
        int age = getUser().getAge();
        int auditFull = sum1 + sum2 + sum3 + sum4 + sum5 + sum6 + sum7 + sum8 + sum9 + sum10;
        int weekTotal = consumoDoses;
        
        if((auditFull <= 17) && ((this.getUser().getGender() == 'F' && consumoDias <= 1) || (this.getUser().getGender() == 'M' && consumoDias <= 2)) && ((this.getUser().getGender() == 'F' && weekTotal <= 5) ||(this.getUser().getGender() == 'M' && weekTotal <= 10))){
            if(this.getUser().getGender() == 'M' && age <= 65){
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml";
            }else if((this.getUser().getGender() == 'M' && age > 65) || this.getUser().getGender() == 'F'){
                return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml";
            }
        }else if((auditFull <= 17) && ((this.getUser().getGender() == 'F' && consumoDias > 1) || (this.getUser().getGender() == 'M' && consumoDias > 2)) && ((this.getUser().getGender() == 'F' && weekTotal > 5) ||(this.getUser().getGender() == 'M' && weekTotal > 10))){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-baixo-risco-limites";
        }else if(auditFull>=18 && auditFull <= 25){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-risco.xhtml";
        }else if(auditFull>=26 && auditFull <= 29){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-nocivo.xhtml";
        }else if(auditFull >= 30 && auditFull <= 50){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-dependencia.xhtml";
        }
        
        try {
            this.getDaoBase().insertOrUpdate(evaluation, this.getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar dados", null));
        }
        return ""; 
    }
    
    public String nextBaixoRisco(){
        User userLocal = getUser();
        int age = userLocal.getAge();
        this.getConsumoDiasDoses();
        int weekTotal = consumoDoses;
        if(((this.getUser().getGender()=='F' && consumoDias > 1) || (this.getUser().getGender()=='M' && consumoDias > 2)) || ((this.getUser().getGender()=='F' && weekTotal > 5) || (this.getUser().getGender()=='M' && weekTotal > 10)))
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-baixo-risco-limites.xhtml";
        else if((this.getUser().getGender()=='F' && consumoDias < 1) || (this.getUser().getGender()=='M' && consumoDias < 2) || (this.getUser().getGender()=='F' && weekTotal < 5) || (this.getUser().getGender()=='M' && weekTotal < 10)){
            if(this.getUser().getGender() == 'M' && age <= 65)
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml";
            else if((this.getUser().getGender() == 'M' && age > 65) || this.getUser().getGender() == 'F' && age > 65)
                return "quanto-voce-bebe-reconmendar-limites-mulheres-e-homens-com-mais-de-65-anos.xhtml";
        }
        return "";
    }
    
    public String audit31() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        if (this.getEvaluation().getSum() <= 3) {
           return this.drinkingLimits();
        } else {
            if (loggedUser()) {
                return "quanto-voce-bebe-sim-beber-uso-audit-7.xhtml?faces-redirect=true";
            }

            return "cadastrar-nova-conta.xhtml?faces-redirect=true";
        }
    }

    public String drinkingLimits() {
        if (this.getEvaluation().getUser().getGender() == 'M' && this.getEvaluation().getUser().getAge() <= 65) {
            return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
        } else {
            return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
        }
    }


    public String audit71() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-doenca.xhtml?faces-redirect=true";
    }

    public String screenForAlcoholUseDisorders() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        if (evaluation.screen()) {
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-doenca-sim-cuidado.xhtml?faces-redirect=true";
        } else {
            return this.drinkingLimits();
        }
    }
    
    public void continueEvaluation() throws IOException{
        System.out.println(evaluation.getDependencia());
        if(evaluation.getDependencia() == 1){
            FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-parar-apoio-intro.xhtml");
            //return "estrategia-parar-apoio-intro.xhtml";
        }
        else{
            System.out.println("POPUP");
        }
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String teste1(){ 
        System.out.println("testeee");
        return "index.xhtml";
    }

    /*public String getGender() {
    if (loggedUser()) {
    return String.valueOf(this.getUser().getGender());
    }
    return gender;
    }*/
    
    public Integer getGender() {
        return gender;
    }
    

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getDrink() {
        return drink;
    }

    public Boolean getDrinkBoolean() {
        if (this.drink == 1) {
            return true;
        } else if (this.drink == 0) {
            return false;
        } else {
            return null;
        }
    }

    public void setDrink(Integer drink) {
        this.drink = drink;
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

    public boolean sameDate(Calendar firstDate, Calendar secondDate) {
        return firstDate.get(Calendar.DAY_OF_MONTH) == secondDate.get(Calendar.DAY_OF_MONTH)
                && firstDate.get(Calendar.MONTH) == secondDate.get(Calendar.MONTH)
                && firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR);
    }

    public boolean isContinueEvaluation() {
        return continueEvaluation;
    }

    public void setContinueEvaluation(boolean continueEvaluation) {
        this.continueEvaluation = continueEvaluation;
    }
    
    public boolean isWoman(){
        return gender == 1;
        //return getGender().equals('F');
        //eturn this.getUser().getGender()=='F';
    }

    public Integer getPregnant() {
        return pregnant;
    }

    public void setPregnant(Integer pregnant) {
        this.pregnant = pregnant;
    }
    
    public String continueEvaluation1(){
        return "preparando-pros-cons.xhtml";
    }
    
    public void nextPreparado() throws IOException{
        if(evaluation.getPreparado() == 1)
            FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-diminuir-introducao.xhtml");
        else
            FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-parar-apoio-intro.xhtml");
        
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String nextBackPlan(){
        if(evaluation.getBackPlan() == 1)
            return "to cut down or to quit";
        else 
            return "avaliação apos um ano";
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

            if(getEvaluation().getDataComecarPlano() != null && !getEvaluation().getRazoesPlano().trim().isEmpty()){
                paragraph = new Paragraph("Data para começar", f2);
                document.add(paragraph);
                paragraph = new Paragraph( new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getEvaluation().getDataComecarPlano()), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else {
                paragraph.add(new Paragraph("teste1"));
            }
            
            if (getEvaluation().getRazoesPlano() != null && !getEvaluation().getRazoesPlano().trim().isEmpty()){
                paragraph = new Paragraph(" As razões mais importantes que eu tenho para mudar a forma que bebo são: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getRazoesPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else{
                paragraph = new Paragraph(" ");
                paragraph.add(paragraph);
            }
            
            if (getEvaluation().getEstrategiasPlano() != null && !getEvaluation().getEstrategiasPlano().trim().isEmpty()){
                paragraph = new Paragraph("Eu irei usar as seguintes estratégias: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getEstrategiasPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else{
                paragraph.add(new Paragraph(" "));
            }
            
            if (getEvaluation().getPessoasPlano() != null && !getEvaluation().getPessoasPlano().trim().isEmpty()){
                paragraph = new Paragraph("As pessoas que podem me ajudar são: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getPessoasPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else{
                paragraph.add(new Paragraph(" "));
            }
            
            if (getEvaluation().getSinaisSucessoPlano() != null || getEvaluation().getSinaisSucessoPlano().trim().isEmpty()){
                paragraph = new Paragraph("Eu saberei que meu plano está funcionando quando: ", f2);
                document.add(paragraph);
                paragraph = new Paragraph(getEvaluation().getSinaisSucessoPlano(), f3);
                document.add(paragraph);
                document.add(Chunk.NEWLINE);
            } else{
                paragraph.add(new Paragraph(" "));
            }
            
            if(getEvaluation().getPossiveisDificuladesPlano() != null || getEvaluation().getPossiveisDificuladesPlano().trim().isEmpty()){
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
    
    
    public void estrategiaRegistro() {
        if(getUser().getGender() == 'F'){
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-diminuir-registro-eletronico-meta-mulher.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else{
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("estrategia-diminuir-registro-eletronico-meta-homem.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String nextRegEleManAndWoman(){
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "estrategia-diminuir-registro-eletronico.xhtml";
    }
    
    
    

}

