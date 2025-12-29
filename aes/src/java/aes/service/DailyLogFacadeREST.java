/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.DailyLog;
import aes.model.Record;
import aes.model.User;
import aes.persistence.DailyLogDAO;
import aes.persistence.RecordDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@Path("secured/dailylog")
@TransactionManagement(TransactionManagementType.BEAN)
public class DailyLogFacadeREST extends AbstractFacade<DailyLog> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private DailyLogDAO dailyLogDAO;
    private RecordDAO recordDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public DailyLogFacadeREST() {
        super(DailyLog.class);
        try {
            dailyLogDAO = new DailyLogDAO();
            recordDao =  new RecordDAO();
        } catch (NamingException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Path("editOrCreate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editOrCreate(DailyLog entity) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            if (entity == null) {
                Logger.getLogger(DailyLogFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_DAILY_LOG reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            Record record = recordDao.find(entity.getRecord().getId(), em);
            
            if (record == null || record.getUser() == null) {
                Logger.getLogger(DailyLogFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_DAILY_LOG reason=INVALID_DATA "
                      + "actorUserId={0}",
                        loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID_DATA")
                    .build();
            }
            
            r = securityHelper.requireSameUser(loggedUser, record.getUser().getId());
            if (r != null) return r;
            
            String action;
            boolean edited = dailyLogDAO.edit(entity, em);

            if (edited) {
                action = "edit";
            } else {
                dailyLogDAO.insert(entity, em);
                action = "create";
            }

            return Response.ok(new JSONObject().put("action", action).toString()).build();
        } catch (JSONException | SQLException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    /*@DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        try {
            //super.remove(super.find(id));
            dailyLogDAO.delete(dailyLogDAO.find(id, em), em);
        } catch (SQLException ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    */

    @GET
    @Path("find/{recordId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("recordId") Long recordId) {
        try {     
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            Record record = recordDao.find(recordId, em);
            
            r = securityHelper.requireSameUser(loggedUser, record.getUser().getId());
            if (r != null) return r;

            List<DailyLog> dailyLogs = dailyLogDAO.find(record.getId(), em);

            if (dailyLogs.isEmpty()) {
                Logger.getLogger(DailyLogFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_DAILYLOG_FIND reason=TARGET_OBJECT_NOT_FOUND "
                      + "actorUserId={0}",
                        loggedUser.getId());

                return Response.status(Response.Status.NOT_FOUND)
                        .entity("TARGET_OBJECT_NOT_FOUND")
                        .build();
            }

            r = securityHelper.requireSameUser(loggedUser, dailyLogs.get(0).getRecord().getUser().getId());
            if (r != null) return r;

            return Response.ok(dailyLogs).build();
        } catch (Exception ex) {
            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
