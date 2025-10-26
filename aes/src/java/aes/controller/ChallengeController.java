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

@Named("challengeController")
@RequestScoped
public class ChallengeController extends BaseController {
    
    public class Challenge {
        
        private int id;
        private String prefix;
        private String type;
        private String baseValue;
        private String modifier;

        public Challenge() {
        }

        public Challenge(int id, String prefix, String type, String baseValue, String modifier) {
            this.id = id;
            this.prefix = prefix;
            this.type = type;
            this.baseValue = baseValue;
            this.modifier = modifier;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(String baseValue) {
            this.baseValue = baseValue;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }
    }
    
    public List<Challenge> challengeList(Locale locale){
        List<Challenge> challenges = new ArrayList<>();
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
                    return challenges; 
                }
                properties.load(input);
            }
            
            boolean continueSearch = true;
            int counter = 1;
            Challenge challenge;
            
            while(continueSearch) {
                challenge = new Challenge(0,"","","","");
                String key = properties.getProperty("challenge.prefix." + counter, "");
                if (!key.isEmpty()) {
                    challenge.setPrefix(key);
                    challenge.setId(counter);
                }
                key = properties.getProperty("challenge.base.value." + counter, "");
                if (!key.isEmpty()) {
                    challenge.setBaseValue(key);
                }
                key = properties.getProperty("challenge.modifier." + counter, "");
                if (!key.isEmpty()) {
                    challenge.setModifier(key);
                }
                key = properties.getProperty("challenge.type." + counter, "");
                if (!key.isEmpty()) {
                    challenge.setType(key);
                }
                if (challenge.getId() != 0) {
                    challenges.add(challenge);
                } else {
                    continueSearch = false;
                }
                counter++;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(TipController.class.getName()).log(Level.SEVERE, "Erro ao carregar o arquivo .properties", ex);
        }
    
        return challenges;
    }
}
