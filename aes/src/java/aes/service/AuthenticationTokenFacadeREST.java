/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;
import aes.model.AuthenticationToken;
import aes.model.User;
import aes.persistence.AuthenticationTokenDAO;
import aes.persistence.UserDAO;
import aes.utility.Encrypter;
import aes.utility.Secured;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonBuilderFactory;
import javax.ws.rs.core.Request;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import java.util.Collections;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;


/**
 *
 * @author bruno
 */
@Stateless
@Path("authenticate")
@TransactionManagement(TransactionManagementType.BEAN)
public class AuthenticationTokenFacadeREST extends AbstractFacade<AuthenticationToken> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private UserDAO userDAO;
    private AuthenticationTokenDAO authenticationTokenDAO;

    @Context
    SecurityContext securityContext;

    public AuthenticationTokenFacadeREST() {
        super(AuthenticationToken.class);
        try {
            userDAO = new UserDAO();
            authenticationTokenDAO = new AuthenticationTokenDAO();
        } catch (NamingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("{email}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authUser(@PathParam("email") String e, @PathParam("password") String p) throws InvalidKeyException {

        try {
            String clientEncriptedHexPassword = p;
            String decriptedPassword = Encrypter.decrypt(clientEncriptedHexPassword);

            User user = userDAO.checkCredentials(e, decriptedPassword, em);

            if (user != null) {
                String token = authenticationTokenDAO.issueToken(user, em);
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + e + "' logou no sistema.");
                return Response.ok(token).build();

            } else {

                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + e + "' não conseguiu logar.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

        } catch (Exception exp) {

            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, null, exp);
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + e + "' não conseguiu logar.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @DELETE
    @Path("secured/logout/{token}")
    @Secured
    public Response logout(@PathParam("token") String token) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();

            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, new StringBuffer(userEmail).append(" está deslogando do sistema.").toString());

            authenticationTokenDAO.revokeToken(token, userEmail, em);

            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, new StringBuffer(userEmail).append(" deslogou do sistema.").toString());

            return Response.ok().build();
        } catch (NoResultException e) {
            return Response.ok().build(); //se o token não existe ou já foi deletado ignoro o erro
        } catch (SQLException e) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    // Método para desabilitar verificação SSL (somente para teste local)
    private void disableSSLVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Desabilita verificação do hostname
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    @POST
    @Path("/verify-recaptcha")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyRecaptcha(JsonObject input) {
        // Pega o token enviado pelo frontend
        String token = input.getString("token", null);
        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                            .add("success", false)
                            .add("message", "Token não fornecido")
                            .build())
                    .build();
        }

        // Sua secret key do reCAPTCHA - confira se está correta e ativa
        String secret = "6LfDt5wrAAAAAG8pAV8-XtL4mn1J4h1EBwNkZlBl";

        try {
            // DESABILITA VERIFICAÇÃO SSL - só para teste local
            disableSSLVerification();

            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // Monta os parâmetros URL-encoded
            String params = "secret=" + java.net.URLEncoder.encode(secret, "UTF-8") +
                    "&response=" + java.net.URLEncoder.encode(token, "UTF-8");

            // Escreve parâmetros no corpo da requisição POST
            try (OutputStream os = conn.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(params);
                writer.flush();
            }

            // Lê a resposta do Google
            try (InputStream is = conn.getInputStream();
                 JsonReader jsonReader = Json.createReader(is)) {

                JsonObject jsonObject = jsonReader.readObject();

                boolean success = jsonObject.getBoolean("success", false);

                JsonObjectBuilder responseBuilder = Json.createObjectBuilder()
                        .add("success", success);

                if (jsonObject.containsKey("score")) {
                    responseBuilder.add("score", jsonObject.getJsonNumber("score").doubleValue());
                }

                if (jsonObject.containsKey("hostname")) {
                    responseBuilder.add("hostname", jsonObject.getString("hostname"));
                }

                if (jsonObject.containsKey("error-codes")) {
                    responseBuilder.add("error-codes", jsonObject.getJsonArray("error-codes"));
                }

                return Response.ok(responseBuilder.build()).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Json.createObjectBuilder()
                            .add("success", false)
                            .add("message", "Erro ao verificar reCAPTCHA")
                            .add("error", e.getMessage())
                            .build())
                    .build();
        }
    }
}
