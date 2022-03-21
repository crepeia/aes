/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AppSuggestion;
import aes.utility.Secured;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Stateless
@Path("app-properties/")
public class AppPropertiesFacadeREST {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public AppPropertiesFacadeREST() {
    }


    @GET
    @Path("get-latest-app-version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLatestAppVersion() {
      Object properties = em.createNativeQuery(
            "SELECT latest_app_version FROM app_properties" ).getSingleResult();
      
        System.out.println();
      return (String)properties;
    }

    
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
