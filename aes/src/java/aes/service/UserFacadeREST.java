/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.controller.ContactController;
import aes.controller.UserController;
import aes.model.User;
import aes.utility.Encrypter;
import aes.utility.Secured;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author bruno
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("secured/user")
public class UserFacadeREST extends AbstractFacade<User> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    @Inject
    private ContactController contactController;
    
    @Resource
    private UserTransaction userTransaction;
    

    @Context
    SecurityContext securityContext;
    
    public UserFacadeREST() {
        super(User.class);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{password}")
    public Response createUser(User entity, @PathParam("password") String p) throws DecoderException, UnsupportedEncodingException {
        List<User> userList = em.createQuery("SELECT u FROM User u WHERE u.email=:e").setParameter("e", entity.getEmail()).getResultList();
        
        if (!userList.isEmpty()) {
            return Response.status(Response.Status.CONFLICT).build();
        } else {
            try {
                //String p = Hex.encodeHexString( entity.getPassword() );
                byte[] b =  Hex.decodeHex(p.toCharArray());
                entity.setPassword(b);
                //byte[] b =  Hex.decodeHex(Arrays.toString(entity.getPassword()).toCharArray());
                userTransaction.begin();
                super.create(entity);
                userTransaction.commit();

                contactController.sendSignUpEmail(entity);
                if (entity.isReceiveEmails()) {
                    contactController.scheduleTipsEmail(entity);
                    contactController.scheduleDiaryReminderEmail(entity, new Date());
                    contactController.scheduleWeeklyEmail(entity, new Date());
                }
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + entity.getEmail() + "'cadastrou no sistema.");
             } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Response.ok(entity).build();
    }
    
    /*
    @POST
    @Path("createUnregistered")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUnregistered(User entity) {
        List<User> userList = em.createQuery("SELECT u FROM User u WHERE u.email=:e").setParameter("e", entity.getEmail()).getResultList();
        try {
            if(!userList.isEmpty()){

                if(userList.get(0).isRegistration_complete()){
                    String error = "{\"error\": \"EmailInUseLogin\"}";
                    return Response.status(Response.Status.CONFLICT).entity(error).build();
                } else {
                    entity.setId(userList.get(0).getId());
                    userTransaction.begin();
                    super.edit(entity);
                    userTransaction.commit();
                }

            } else {
                userTransaction.begin();
                super.create(entity);
                userTransaction.commit();
            }
            return Response.ok().entity(entity).build();

        } catch(Exception e){
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    */
    
    @PUT
    @Path("/toggleConsultant/{id}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public User toggleConsultant(@PathParam("id") Long id) {
        String userEmail = securityContext.getUserPrincipal().getName();
        try{
            User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                                .setParameter("email", userEmail)
                                .getSingleResult();
            u.setConsultant(!u.isConsultant());
            userTransaction.begin();
            super.edit(u);
            userTransaction.commit();
            return u;
        }catch(Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    
    @PUT
    @Path("/setInRanking")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User setInRanking(User entity) {
        String userEmail = securityContext.getUserPrincipal().getName();
        System.out.println("aes.service.UserFacadeREST.setInRanking()");
        try{
            
            User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                                .setParameter("email", userEmail)
                                .getSingleResult();
            System.out.println(u.getEmail());
            System.out.println(entity.isInRanking());
            System.out.println(entity.getNickname());

            u.setInRanking(entity.isInRanking());
            u.setNickname(entity.getNickname());

            userTransaction.begin();
            super.edit(u);
            userTransaction.commit();
            return u;
        }catch(Exception e) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    

/*
    

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Long id, User entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public User find(@PathParam("id") Long id) {
        return super.find(id);
    }

    

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<User> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }
*/
    @GET
    @Path("count")
    @Secured
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @GET
    @Secured
    @Path("login/{token}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Override
    public User login(@PathParam("token") String tkn) {
        return super.login(tkn);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
