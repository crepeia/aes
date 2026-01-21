/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.TipUser;
import aes.model.TipUserKey;
import aes.model.User;
import aes.persistence.TipUserDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@Path("tipuser")
@TransactionManagement(TransactionManagementType.BEAN)
public class TipUserFacadeREST extends AbstractFacade<TipUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    @Context
    SecurityContext securityContext;
    
    private TipUserDAO tipUserDAO;
    
    @Inject
    private SecurityContextHelper securityHelper;

    private TipUserKey getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;tipId=tipIdValue;userId=userIdValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        aes.model.TipUserKey key = new aes.model.TipUserKey();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> tipId = map.get("tipId");
        if (tipId != null && !tipId.isEmpty()) {
            key.setTipId(Long.parseLong(tipId.get(0)));
        }
        java.util.List<String> userId = map.get("userId");
        if (userId != null && !userId.isEmpty()) {
            key.setUserId(Long.parseLong(userId.get(0)));
        }
        return key;
    }

    public TipUserFacadeREST() {
        super(TipUser.class);
        try {
            tipUserDAO = new TipUserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Path("createTip")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTip(String body) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Long tipId = Long.parseLong(body);
            
            TipUser entity = tipUserDAO.findByUserAndTip(loggedUser.getId(), tipId, em);

            if(entity == null){
                TipUser newTipUser = new TipUser();
                
                TipUserKey key = new TipUserKey();
                key.setTipId(tipId);
                key.setUserId(loggedUser.getId());
                
                newTipUser.setId(key);
                newTipUser.setUser(loggedUser);
                newTipUser.setDateCreated(new Date());
                
                tipUserDAO.createTip(newTipUser, em);
                
                return Response.status(Response.Status.CREATED).build();
            }
            
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @PUT
    @Path("like")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response like(String body) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Long tipId = Long.parseLong(body);
            
            TipUser entity = tipUserDAO.findByUserAndTip(loggedUser.getId(), tipId, em);
            
            if (entity == null) {
                Logger.getLogger(TipUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_TIP_USER_LIKE reason=TARGET_OBJECT_NOT_FOUND "
                        + "actorUserId={0} tipId={1}",
                        new Object[]{loggedUser.getId(), tipId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            tipUserDAO.like(entity, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("dislike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dislike(String body) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Long tipId = Long.parseLong(body);
            
            TipUser entity = tipUserDAO.findByUserAndTip(loggedUser.getId(), tipId, em);
            
            if (entity == null) {
                Logger.getLogger(TipUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_TIP_USER_DESLIKE reason=TARGET_OBJECT_NOT_FOUND "
                        + "actorUserId={0} tipId={1}",
                        new Object[]{loggedUser.getId(), tipId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            tipUserDAO.dislike(entity, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("read")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces( MediaType.APPLICATION_JSON)
    public Response read(String body) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Long tipId = Long.parseLong(body);
            
            TipUser entity = tipUserDAO.findByUserAndTip(loggedUser.getId(), tipId, em);
            
            if (entity == null) {
                Logger.getLogger(TipUserFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_TIP_USER_READ reason=TARGET_OBJECT_NOT_FOUND "
                        + "actorUserId={0} tipId={1}",
                        new Object[]{loggedUser.getId(), tipId});
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }
            
            tipUserDAO.read(entity, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException ex) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findByUser")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByUser() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            List<TipUser> list = tipUserDAO.findByUser(loggedUser.getId(), em);
            return Response.ok().entity(list).build();
        } catch (Exception ex) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    /*
    @GET
    @Path("secured/{startDate}/{endDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findByDate(@PathParam("startDate") String sd, @PathParam("endDate") String ed) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
            List<TipUser> list = tipUserDAO.findByDate(sd, ed, userEmail, em);
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            Logger.getLogger(TipUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(tipUserDAO.count(em));
    }
    */

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
