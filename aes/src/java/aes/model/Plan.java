/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    
    private String htmlTemplate;
    private ByteArrayOutputStream pdf;
    
    
    public Plan(Evaluation evaluation){
        try {
            String subtitle[] = new String[6];
            String content[] = new String[6];
            String title = "Meu Plano";
            subtitle[0] = "Data para começar";
            subtitle[1] = "As razões mais importantes que eu tenho para mudar a forma que bebo são:";
            subtitle[2] = "Eu irei usar as seguintes estratégias:" ;
            subtitle[3] = "As pessoas que podem me ajudar são:" ;
            subtitle[4] = "Eu saberei que meu plano está funcionando quando:";
            subtitle[5] = "O que pode interferir e como posso lidar com estas situações:";
            if(evaluation.getDataComecarPlano() != null){
                content[0] = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(evaluation.getDataComecarPlano());
            }
            else{
                content[0] = "";
            }
            content[1] = evaluation.getRazoesPlano();
            content[2] = evaluation.getEstrategiasPlano();
            content[3] = evaluation.getPessoasPlano();
            content[4] = evaluation.getSinaisSucessoPlano();
            content[5] = evaluation.getPossiveisDificuladesPlano();
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String absolutePath = servletContext.getRealPath("/resources/default/templates/plan-template.html");
            byte[] encoded = Files.readAllBytes(Paths.get(absolutePath));
            htmlTemplate =  new String(encoded, StandardCharsets.UTF_8);
            
            htmlTemplate = htmlTemplate.replace("#title#", title);
            for(int i = 0; i < 6; i++){
                if (content[i] != null && !content[i].trim().isEmpty()){
                    htmlTemplate = htmlTemplate.replace("#subtitle" + (i+1) + "#", subtitle[i]);
                    htmlTemplate = htmlTemplate.replace("#content" + (i+1) + "#", content[i]);
                } else {
                    htmlTemplate = htmlTemplate.replace("#subtitle" + (i+1) + "#", "");
                    htmlTemplate = htmlTemplate.replace("#content" + (i+1) + "#", "");
                }
            }
            
            pdf = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, pdf);
            document.open();
            document.addTitle("Meu Plano - Álcool e Saúde");
            document.addAuthor("alcoolesaude.com.br");
            InputStream is = new ByteArrayInputStream(htmlTemplate.getBytes());           
            XMLWorkerHelper.getInstance().parseXHtml(pdfWriter, document, is);
            document.close();
            pdf.close();
           
        } catch (IOException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    public ByteArrayOutputStream getPdf() {
        return pdf;
    }

    public void setPdf(ByteArrayOutputStream pdf) {
        this.pdf = pdf;
    }   
    
}