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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

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
           // System.setProperty("javax.net.ssl.trustStore", certificatesTrustStorePath);
            
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
    
    public void sendPushNotification(String expoPushToken) {
        HttpURLConnection conn = null;
        try {
            
            // Ignorar validação SSL
            TrustManager[] trustAllCerts;
            trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ExpoNotification.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                sc.init(null, trustAllCerts, new SecureRandom());
            } catch (KeyManagementException ex) {
                Logger.getLogger(ExpoNotification.class.getName()).log(Level.SEVERE, null, ex);
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
            URL url = new URL("https://exp.host/--/api/v2/push/send");
            
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-encoding", "gzip, deflate");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String message = Json.createObjectBuilder()
                .add("to", expoPushToken)
                .add("sound", "default")
                .add("title", "Original Title")
                .add("body", "And here is the body!")
                .add("data", Json.createObjectBuilder().add("someData", "goes here").build())
                .build()
                .toString();
            
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                byte[] input = message.getBytes(StandardCharsets.UTF_8);
                wr.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if(responseCode >= 200 && responseCode < 300) {
                System.out.println("Notificação enviada com sucesso!");
            } else {
                System.err.println("Erro ao enviar notificação. Código: " + responseCode);
            }
        } catch (IOException e) {
            System.out.println("ERRO: " + e);
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }
}