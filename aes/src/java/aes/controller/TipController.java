package aes.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;

import javax.inject.Named;

/**
 *
 * @author bruno
 */

@Named("tipController")
@RequestScoped
public class TipController extends BaseController {
    
    public class Tip {
        private int id;
        private String title;
        private String description;
        
        public Tip() {}

        public Tip(int id, String title, String description) {
            this.id = id;
            this.title = title;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    public List<Tip> tipList(Locale locale){
        List<Tip> tips = new ArrayList<>();
        String language = locale.getLanguage();
        Properties properties = new Properties();
        String propertiesPath = this.getMessagesPath() + "_pt.properties";

        if (language.equals("en")) {
            propertiesPath = this.getMessagesPath() + "_en.properties";
        }
        
        if (language.equals("es")) {
            propertiesPath = this.getMessagesPath() + "_es.properties";
        }
        

        try {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesPath)) {
                if (input == null) {
                    System.err.println("Arquivo não encontrado no classpath: " + propertiesPath);
                    return tips; 
                }
                properties.load(input);
            }
            
            boolean continueSearch = true;
            int counter = 1;
            Tip tip;
            
            while(continueSearch) {
                tip = new Tip(0,"","");
                String key = properties.getProperty("tip.title." + counter, "");
                if (!key.isEmpty()) {
                    tip.setTitle(key);
                }
                key = properties.getProperty("tip.description." + counter, "");
                if (!key.isEmpty()) {
                    tip.setDescription(key);
                    tip.setId(counter);
                }
                if (tip.getId() != 0) {
                    tips.add(tip);
                } else {
                    continueSearch = false;
                }
                counter++;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, "Erro ao carregar o arquivo .properties", ex);
        }
    
        return tips;
    }
}
