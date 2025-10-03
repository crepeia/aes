package aes.service;

import aes.persistence.NonceDAO;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 *
 * @author luansb
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("nonce")
public class NonceServiceFacadeREST {
    private NonceDAO nonceDao;
    
    public NonceServiceFacadeREST() {
        try {
            nonceDao = new NonceDAO();
        } catch (NamingException ex) {
            Logger.getLogger(NonceServiceFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @GET
    @Path("generate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateNonce() {
        try {
            String nonce = nonceDao.createNonce();
            return Response.ok(nonce).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NonceServiceFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @POST
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateNonce(String nonce) {
        try {
            if (nonce == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"valid\": false, \"error\": \"missing nonce\"}")
                        .build();
            }
            boolean valid = nonceDao.validateNonce(nonce);
            return Response.ok("{\"valid\": " + valid + "}").build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(NonceServiceFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"valid\": false, \"error\": \"server error\"}")
                    .build();
        }
    }
}
