/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import aes.model.Tip;
import aes.model.TipUser;
import aes.model.TipUserKey;
import aes.model.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
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
@Path("tipuser")
public class TipUserFacadeREST extends AbstractFacade<TipUser> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    @Context 
    private HttpServletRequest httpRequest;

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
            key.setTipId(new java.lang.Long(tipId.get(0)));
        }
        java.util.List<String> userId = map.get("userId");
        if (userId != null && !userId.isEmpty()) {
            key.setUserId(new java.lang.Long(userId.get(0)));
        }
        return key;
    }

    public TipUserFacadeREST() {
        super(TipUser.class);
    }

    @POST
    @Path("createTip")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTip(TipUser entity) {
        try {
            entity.setUser(em.find(User.class, entity.getId().getUserId()));
            entity.setTip(em.find(Tip.class, entity.getId().getTipId()));
            
            if(entity.getDateCreated()== null){
                entity.setDateCreated(new Date());
            }
            
            super.create(entity);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        }
    }

    @PUT
    @Path("like")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces( MediaType.APPLICATION_JSON)
    public TipUser like(TipUser entity) {
        TipUser newEntity = super.find(entity.getId());
        if(newEntity.isLiked() != null && newEntity.isLiked() == true){
            newEntity.setLiked(null);
        } else {
            newEntity.setLiked(true);
        }
        super.edit(newEntity);
        return newEntity;
    }
    
    @PUT
    @Path("dislike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces( MediaType.APPLICATION_JSON)
    public TipUser dislike(TipUser entity) {
        TipUser newEntity = super.find(entity.getId());
        if(newEntity.isLiked() != null && newEntity.isLiked() == false){
            newEntity.setLiked(null);
        } else {
            newEntity.setLiked(false);
        }
        super.edit(newEntity);
        return newEntity;
    }
    
    @PUT
    @Path("unlike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces( MediaType.APPLICATION_JSON)
    public TipUser unlike(TipUser entity) {
        TipUser newEntity = super.find(entity.getId());
        newEntity.setLiked(null);
        super.edit(newEntity);
        return newEntity;
    }
    
    @PUT
    @Path("read")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces( MediaType.APPLICATION_JSON)
    public TipUser read(TipUser entity) {
        TipUser newEntity = super.find(entity.getId());
        newEntity.setReadByUser(true);
        super.edit(newEntity);
        return newEntity;
    }
    
    @GET
    @Path("find/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TipUser> findByUser(@PathParam("userId") String uId) {
        try {
            
        return getEntityManager().createQuery("SELECT tu FROM TipUser tu WHERE tu.user.id=:userId")
                .setParameter("userId", Long.parseLong(uId))
                .getResultList();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Path("secured/{startDate}/{endDate}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TipUser> findByDate(@PathParam("startDate") String sd, @PathParam("endDate") String ed) {
        try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(sd);   
        Date endDate = sdf.parse(ed);

        String userEmail = httpRequest.getAttribute("userEmail").toString();
        
        return getEntityManager().createQuery("SELECT tu FROM TipUser tu WHERE tu.user.email=:email AND (tu.dateCreated BETWEEN :start AND :end)")
                .setParameter("email", userEmail)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
        /*
    @POST
    @Path("createId/{id}")
    public Response createId(@PathParam("id") PathSegment id) {
        try {
            TipUserKey k = getPrimaryKey(id);
            User usr = em.find(User.class, k.getUserId());
            Tip tip = em.find(Tip.class, k.getTipId());
            
            TipUser tu = new TipUser();
            tu.setUser(usr);
            tu.setTip(tip);
            tu.setDataEnvio(new Date());
            tu.setLiked(false);
            tu.setId(k);
            
            super.create(tu);
            return Response.status(Response.Status.OK).entity(tu).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();

        }
    }
*/
}
