/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author bruno
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {
/*
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        // following code can be used to customize Jersey 1.x JSON provider:
        try {
            Class jacksonProvider = Class.forName("org.codehaus.jackson.jaxrs.JacksonJsonProvider");
            resources.add(jacksonProvider);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        addRestResourceClasses(resources);
        return resources;
    }
    */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        // following code can be used to customize Jersey 1.x JSON provider:
        resources.add(JacksonFeature.class);
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(aes.service.AgendaAppointmentFacadeREST.class);
        resources.add(aes.service.AgendaAvailableFacadeREST.class);
        resources.add(aes.service.AppNavigationFacadeREST.class);
        resources.add(aes.service.AppPropertiesFacadeREST.class);
        resources.add(aes.service.AppSuggestionFacadeREST.class);
        resources.add(aes.service.AuthenticationFilter.class);
        resources.add(aes.service.AuthenticationTokenFacadeREST.class);
        resources.add(aes.service.ChallengeFacadeREST.class);
        resources.add(aes.service.ChallengeUserFacadeREST.class);
        resources.add(aes.service.ChatFacadeREST.class);
        resources.add(aes.service.ChatbotResource.class);
        resources.add(aes.service.ContactFacadeREST.class);
        resources.add(aes.service.DailyLogFacadeREST.class);
        resources.add(aes.service.EvaluationFacadeREST.class);
        resources.add(aes.service.MedalUserFacadeREST.class);
        resources.add(aes.service.MessageFacadeREST.class);
        resources.add(aes.service.MobileOptionsFacadeREST.class);
        resources.add(aes.service.NotificationFacadeREST.class);
        resources.add(aes.service.PageRatingFacadeREST.class);
        resources.add(aes.service.QuestionFacadeREST.class);
        resources.add(aes.service.QuestionUserFacadeREST.class);
        resources.add(aes.service.RecordFacadeREST.class);
        resources.add(aes.service.TipFacadeREST.class);
        resources.add(aes.service.TipUserFacadeREST.class);
        resources.add(aes.service.TitleUserFacadeREST.class);
        resources.add(aes.service.UserFacadeREST.class);
        resources.add(aes.utility.ObjectMapperContextResolver.class);
    }
    
}
