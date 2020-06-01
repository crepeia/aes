/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import aes.model.TipUser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.json.Json;

/**
 *
 * @author bruno
 */
public class ExpoNotification {
    public ExpoNotification(){
        
    }
    
    public void send(String expoToken, TipUser tipUser){
        try {
            //String certificatesTrustStorePath = "C:/Program Files/Java/jdk1.8.0_221/jre/lib/security/cacerts";
            //System.setProperty("javax.net.ssl.trustStore", certificatesTrustStorePath);
            String urlParameters  = "param1=a&param2=b&param3=c";
            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
            int    postDataLength = postData.length;
            String request        = "https://exp.host/--/api/v2/push/send";
            URL    url = new URL( request );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();           
            
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty("host", "exp.host"); 
            conn.setRequestProperty("Content-Type", "application/json"); 
            conn.setRequestProperty("accept-encoding", "gzip, deflate");
            conn.setRequestProperty("accept", "application/json");
            conn.setUseCaches( false );
            //conn.setInstanceFollowRedirects(true);
            conn.setDoOutput( true );
            //System.out.println(conn.getHeaderFields());
            //System.out.println(conn.getURL());
            //System.out.println(conn.getHeaderFields());
            String jsonScreen = Json.createObjectBuilder()
                    .add("screen", "Dicas")
                    .build().toString();
            String jsonString = Json.createObjectBuilder()
                    .add("to", expoToken)
                    .add("title", tipUser.getTip().getTitle())
                    .add("body", tipUser.getTip().getDescription())
                    .add("data", jsonScreen)
                    .build().toString();
            
            try(DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                byte[] input = jsonString.getBytes("utf-8");
                wr.write(input, 0, input.length);           
            }
            int responseCode = conn.getResponseCode();
            
            try(BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "utf-8"))) {
              StringBuilder response = new StringBuilder();
              String responseLine = null;
              while ((responseLine = br.readLine()) != null) {
                  response.append(responseLine.trim());
              }
              System.out.println(response.toString());
          }
            
            System.out.println(conn.getHeaderFields());
        } catch (MalformedURLException ex) {
            Logger.getLogger(ExpoNotification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExpoNotification.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}