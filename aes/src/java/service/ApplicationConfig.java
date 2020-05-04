/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

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
        resources.add(service.AuthenticationFilter.class);
        resources.add(service.AuthenticationTokenFacadeREST.class);
        resources.add(service.ChallengeFacadeREST.class);
        resources.add(service.ChallengeUserFacadeREST.class);
        resources.add(service.DailyLogFacadeREST.class);
        resources.add(service.EvaluationFacadeREST.class);
        resources.add(service.MobileOptionsFacadeREST.class);
        resources.add(service.RecordFacadeREST.class);
        resources.add(service.TipFacadeREST.class);
        resources.add(service.TipUserFacadeREST.class);
        resources.add(service.UserFacadeREST.class);
    }
    
}
