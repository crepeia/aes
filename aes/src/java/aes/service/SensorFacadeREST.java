package aes.service;

import aes.model.SensorData;
import aes.model.User;
import aes.persistence.SensorDataDAO;
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
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("secured/sensor")
public class SensorFacadeREST extends AbstractFacade<SensorData> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private SensorDataDAO sensorDataDAO;

    @Inject
    private SecurityContextHelper securityHelper;

    public SensorFacadeREST() {
        super(SensorData.class);
        try {
            sensorDataDAO = new SensorDataDAO();
        } catch (NamingException ex) {
            Logger.getLogger(SensorFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    // Rota que recebe uma LISTA de leituras (Batch) para economizar bateria
    @POST
    @Path("/submitBatch")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitSensorDataBatch(List<SensorData> payloadList) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            if (payloadList == null || payloadList.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("{\"error\":\"Lista de sensores vazia\"}")
                               .build();
            }

            // Grava cada leitura associada ao utilizador logado
            for (SensorData data : payloadList) {
                data.setUser(loggedUser);
                sensorDataDAO.saveSensorData(data, em);
            }

            Logger.getLogger(SensorFacadeREST.class.getName())
                .log(Level.INFO, "Lote de {0} leituras de sensores gravado para o utilizador ID={1}", 
                     new Object[]{payloadList.size(), loggedUser.getId()});

            return Response.status(Response.Status.CREATED).build();

        } catch (SQLException ex) {
            Logger.getLogger(SensorFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao gravar dados de sensor", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Erro interno ao salvar os dados\"}")
                           .build();
        }
    }
}