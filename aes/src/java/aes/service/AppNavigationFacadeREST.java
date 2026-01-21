/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AppNavigation;
import aes.model.User;
import aes.persistence.AppNavigationDAO;
import aes.persistence.GenericDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Leonorico
 */
@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("appnavigation")
public class AppNavigationFacadeREST extends AbstractFacade<AppNavigation> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AppNavigationDAO appNavigationDao;
    
    @Inject
    private SecurityContextHelper securityHelper;
    
    @Context
    private HttpServletRequest request;

    public AppNavigationFacadeREST() {
        super(AppNavigation.class);
        try {
            appNavigationDao = new AppNavigationDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AppNavigationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    private String getFullIpChain() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();
        
        if (xForwardedFor == null || xForwardedFor.isEmpty()) {
            return remoteAddr;
        } else {
            return xForwardedFor + ", " + remoteAddr;
        }
    }
    
    @Path("saveNavigation")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveNavigation(AppNavigation appNavigation) {
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            String serverIpChain = getFullIpChain();
            String clientIp = appNavigation.getIp();
            
            boolean ipExistsInChain = serverIpChain.contains(clientIp);
            
            if (clientIp != null && !ipExistsInChain) {
                Logger.getLogger(AppNavigationFacadeREST.class.getName())
                    .log(Level.INFO,
                         "IP Divergence | userId={0} | serverChain=[{1}] | clientIp={2}",
                         new Object[]{ loggedUser.getId(), serverIpChain, clientIp });
            }
            
            AppNavigation nav = new AppNavigation();
            nav.setUser(loggedUser);
            nav.setTimeStamp(new Date());
            nav.setIp(serverIpChain);
            nav.setUserAgent(appNavigation.getUserAgent());
            
            appNavigationDao.saveNavigation(nav, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(AppNavigationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
