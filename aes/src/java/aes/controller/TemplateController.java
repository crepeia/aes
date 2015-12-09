/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 *
 * @author thiago
 */
@ManagedBean(name = "templateController")
@ApplicationScoped
public class TemplateController extends BaseController {

    private String contactTemplate;
    private String planTemplate;
    
    
    public TemplateController() {
       
    }


    public String readTemplate(String path) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        String absolutePath = servletContext.getRealPath(path);
        byte[] encoded = Files.readAllBytes(Paths.get(absolutePath));
        String template = new String(encoded, StandardCharsets.UTF_8);
        return template;
    }

    public String fillContactTemplate(String title, String subtitle, String content) {
        try {
            if(contactTemplate == null)
            contactTemplate = readTemplate("/resources/default/templates/contact-template.html");
        } catch (IOException ex) {
            Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String template = contactTemplate;
        if (title != null) {
            template = template.replace("#title#", title);
        }
        if (subtitle != null) {
            template = template.replace("#subtitle#", subtitle);
        }
        if (content != null) {
            template = template.replace("#content#", content);
        }
        return template;

    }

    public String fillPlanTemplate(String content[]) {
        try {
            if(planTemplate == null)
            planTemplate = readTemplate("/resources/default/templates/plan-template.html");
        } catch (IOException ex) {
            Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
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
        if(planTemplate == null){
            System.out.println("null1");
        }
        if(template == null){
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

}
