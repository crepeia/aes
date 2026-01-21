package aes.service;

import aes.model.Tip;
import aes.persistence.TipDAO;
import aes.utility.RESTApiResponse;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.Objects;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@TransactionManagement(TransactionManagementType.BEAN)
/**
 * @Deprecated
 * This class is no longer in use, because tip is not in the databank anymore.
 * Challenge is in .properties file and should be used from there, instead.
**/
@Deprecated
@Path("tip")
public class TipFacadeREST extends AbstractFacade<Tip> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private TipDAO tipDAO;

    public TipFacadeREST() {
        super(Tip.class);
     
        try {
            tipDAO = new TipDAO();
        } catch (NamingException ex) {
            Logger.getLogger(TipFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    }

    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        RESTApiResponse response;
        try {
            response = new RESTApiResponse(tipDAO.listOnce("id", id, em));
            return Response.status(Response.Status.OK).entity(response.getEntityData()).build();
        } catch (SQLException | RuntimeException ex) {
            response = new RESTApiResponse("Ocorre um erro: " + ex);
            Logger.getLogger(TipFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.getMessage()).build();
        }
    }

    @GET
    @Path("findAll")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAllTips() {
        RESTApiResponse response;
        try {
            response = new RESTApiResponse(this.tipDAO.list(em));
            return Response.status(Response.Status.OK).entity(response.getEntityData()).build();
        } catch (SQLException | RuntimeException ex) {
            response = new RESTApiResponse("Ocorre um erro: " + ex);
            Logger.getLogger(TipFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.getMessage()).build();
        }
    }
    
    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response update(Tip tip) {
        RESTApiResponse response;
        try {
            boolean isAnyDescriptionNull = Objects.isNull(tip.getDescriptionPT()) || Objects.isNull(tip.getDescriptionEN()) || Objects.isNull(tip.getDescriptionES());
            if(isAnyDescriptionNull) {
                response = new RESTApiResponse("Informe valores para todos tipos de descrição.", tip);
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }
            boolean isAnyDescriptionEmpty = tip.getDescriptionPT().trim().isEmpty() || tip.getDescriptionEN().trim().isEmpty() || tip.getDescriptionES().trim().isEmpty();
            if(isAnyDescriptionEmpty) {
                response = new RESTApiResponse("Informe valores para todos tipos de descrição.", tip);
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }
            tipDAO.update(tip, em);
            response = new RESTApiResponse("Lembre-se de cadastrar as descrições nas folhas de tradução");
            return Response.status(Response.Status.OK).entity(response.getMessage()).build();
        } catch (SQLException | RuntimeException ex) {
            response = new RESTApiResponse("Ocorreu um erro: " + ex);
            Logger.getLogger(TipFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.getMessage()).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
