/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MediaType;
import org.apache.xml.security.utils.ClassLoaderUtils;

/**
 * REST Web Service
 *
 * @author bruno
 */
@Path("chatbot")
@RequestScoped
public class ChatbotResource {
    
    public class ChatbotMessage {
        public String message;
    }

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ChatbotResource
     */
    public ChatbotResource() {
    }

    /**
     * Retrieves representation of an instance of service.ChatbotResource
     * @param message
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        return null;
    }

    /**
     * PUT method for updating or creating an instance of ChatbotResource
     * @param content representation for the resource
     * @return 
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("python")
    public List<String> scriptPython2(String content) {
        try {
             try {
            Gson g = new Gson();
            ChatbotMessage cm = g.fromJson(content, ChatbotMessage.class);        
            //URL file = Thread.currentThread().getContextClassLoader().getResource("aes/service/testePython.py");
           
            URL resource = ChatbotResource.class.getResource("testePython.py");
            String pathToScript = Paths.get(resource.toURI()).toFile().getAbsolutePath();
            
            String[] cmd = new String[]{"python", pathToScript, cm.message};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            
            InputStream stdout = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout,StandardCharsets.UTF_8));
            List<String> line = new ArrayList<>();
            String returnVal;
            try{
                while((returnVal = reader.readLine()) != null){
                     line.add(returnVal);
                }
             }catch(IOException e){
                   System.out.println("Exception in reading output"+ e.toString());
             }

            
            return line;
            } catch (URISyntaxException ex) {
                Logger.getLogger(ChatbotResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatbotResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
     /**
     * PUT method for updating or creating an instance of ChatbotResource
     * @param content representation for the resource
     * @return 
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("python3")
    public List<String> scriptPython3(String content) {
        try {
             try {
            Gson g = new Gson();
            ChatbotMessage cm = g.fromJson(content, ChatbotMessage.class);        
            //URL file = Thread.currentThread().getContextClassLoader().getResource("aes/service/testePython.py");
           
            URL resource = ChatbotResource.class.getResource("testePython.py");
            String pathToScript = Paths.get(resource.toURI()).toFile().getAbsolutePath();
            
            String[] cmd = new String[]{"python3", pathToScript, cm.message};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            
            InputStream stdout = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout,StandardCharsets.UTF_8));
            List<String> line = new ArrayList<>();
            String returnVal;
            try{
                while((returnVal = reader.readLine()) != null){
                     line.add(returnVal);
                }
             }catch(IOException e){
                   System.out.println("Exception in reading output"+ e.toString());
             }

            
            return line;
            } catch (URISyntaxException ex) {
                Logger.getLogger(ChatbotResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatbotResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
