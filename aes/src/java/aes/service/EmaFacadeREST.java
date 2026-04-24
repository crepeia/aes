package aes.service;

import aes.model.EmaAnswer;
import aes.model.User;
import aes.persistence.EmaAnswerDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
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
@Path("secured/ema")
public class EmaFacadeREST extends AbstractFacade<EmaAnswer> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private EmaAnswerDAO emaAnswerDAO;

    @Inject
    private SecurityContextHelper securityHelper;

    public EmaFacadeREST() {
        super(EmaAnswer.class);
        try {
            emaAnswerDAO = new EmaAnswerDAO();
        } catch (NamingException ex) {
            Logger.getLogger(EmaFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @POST
    @Path("/submit")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitEmaAnswer(EmaAnswer payload) {
        try {
            // 1. Valida se o token foi enviado e é válido
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            // 2. Descobre quem é o utilizador que está a responder
            User loggedUser = securityHelper.getLoggedUser();

            // 3. Vincula a resposta ao utilizador
            payload.setUser(loggedUser);

            // 4. Guarda na base de dados
            emaAnswerDAO.saveAnswer(payload, em);

            Logger.getLogger(EmaFacadeREST.class.getName())
                .log(Level.INFO, "Resposta do EMA gravada com sucesso para o utilizador ID={0}", loggedUser.getId());

            return Response.status(Response.Status.CREATED).build();

        } catch (SQLException ex) {
            Logger.getLogger(EmaFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao gravar EMA", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Erro interno ao salvar os dados\"}")
                           .build();
        }
    }
}