/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;


/**
 *
 * @author thiagorizuti
 */
public class Plan {
    
    private long id;
    private Evaluation evaluation;
    private String htmlTemplate;
    
    public Plan(Evaluation evaluation){
        try {
            this.evaluation = evaluation;
            String content[] = new String[6];
            String subtitle[] = new String[6];
            String title = "Meu Plano";
            subtitle[0] = "Data para começar";
            subtitle[1] = "As razões mais importantes que eu tenho para mudar a forma que bebo são:";
            subtitle[2] = "Eu irei usar as seguintes estratégias:" ;
            subtitle[3] = "As pessoas que podem me ajudar são:" ;
            subtitle[4] = "Eu saberei que meu plano está funcionando quando:";
            subtitle[5] = "O que pode interferir e como posso lidar com estas situações: ";
            content[0] = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(this.evaluation.getDataComecarPlano());
            content[1] = this.evaluation.getRazoesPlano();
            content[2] = this.evaluation.getEstrategiasPlano();
            content[3] = this.evaluation.getPessoasPlano();
            content[4] = this.evaluation.getSinaisSucessoPlano();
            content[5] = this.evaluation.getPossiveisDificuladesPlano();
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String absolutePath = servletContext.getRealPath("/resources/default/email-templates/template.html");
            byte[] encoded = Files.readAllBytes(Paths.get(absolutePath));
            htmlTemplate =  new String(encoded, StandardCharsets.UTF_8);
            
            for(int i = 1; i <= 6; i++){
                if (content[i] != null && !content[i].trim().isEmpty()){
                    htmlTemplate = htmlTemplate.replace("#title" + i + "#", content[i-1]);
                    htmlTemplate = htmlTemplate.replace("#content" + i + "#", content[i-1]);
                } else {
                    htmlTemplate = htmlTemplate.replace("#title" + i + "#", "");
                    htmlTemplate = htmlTemplate.replace("#content" + i + "#", "");
                }
            }
            
           
        } catch (IOException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }
    
}
