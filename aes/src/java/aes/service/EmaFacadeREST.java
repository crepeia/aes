package aes.service;

import aes.model.EmaAnswer;
import aes.model.User;
import aes.persistence.EmaAnswerDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
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

    // Fuso horário em que os timestamps estão armazenados (igual ao @JsonFormat da entidade)
    private static final TimeZone TZ_BRASIL = TimeZone.getTimeZone("America/Sao_Paulo");

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

    // ─────────────────────────────────────────────────────────────────────────
    // POST /submit
    // ─────────────────────────────────────────────────────────────────────────

    @POST
    @Path("/submit")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitEmaAnswer(EmaAnswer payload) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            payload.setUser(loggedUser);

            emaAnswerDAO.saveAnswer(payload, em);

            Logger.getLogger(EmaFacadeREST.class.getName())
                .log(Level.INFO, "Resposta EMA gravada para utilizador ID={0}", loggedUser.getId());

            return Response.status(Response.Status.CREATED).build();

        } catch (SQLException ex) {
            Logger.getLogger(EmaFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao gravar EMA", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Erro interno ao salvar os dados\"}")
                           .build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /checkPeriod/{periodo}
    //
    // {periodo} no formato "YYYY-MM-DD_manha" | "YYYY-MM-DD_tarde" | "YYYY-MM-DD_noite"
    //
    // Regras para "noite":
    //   Sem resposta                              → { respondeuNestePeriodo: false }
    //   Última resposta em madrugada (00h–07h)
    //     E agora é noite real (≥ 20h)            → { respondeuNestePeriodo: false }
    //   Qualquer outro caso                       → { respondeuNestePeriodo: true  }
    //
    // Manhã e tarde: comportamento original (uma resposta bloqueia o período).
    // ─────────────────────────────────────────────────────────────────────────

    @GET
    @Path("/checkPeriod/{periodo}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkEmaPeriod(@PathParam("periodo") String periodo) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            if (periodo.endsWith("_noite")) {
                return checkPeriodoNoite(loggedUser.getId(), periodo);
            }

            // Manhã e tarde: verificação simples (sem distinção de horário)
            boolean jaRespondeu = emaAnswerDAO.checkSeJaRespondeu(loggedUser.getId(), periodo, em);
            return buildCheckResponse(jaRespondeu);

        } catch (Exception ex) {
            Logger.getLogger(EmaFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao verificar período EMA", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Erro interno ao verificar os dados\"}")
                           .build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica específica para o período "noite"
    // ─────────────────────────────────────────────────────────────────────────

    private Response checkPeriodoNoite(Long userId, String periodo) {

        // Busca o timestamp da última resposta de "noite" para esta data.
        // Retorna null se não houver nenhuma resposta ainda.
        Date ultimaResposta = emaAnswerDAO.getLastAnswerTimestamp(userId, periodo, em);

        // Nunca respondeu → pode responder normalmente
        if (ultimaResposta == null) {
            return buildCheckResponse(false);
        }

        // Extrai a hora da última resposta respeitando o fuso de Brasília
        Calendar cal = Calendar.getInstance(TZ_BRASIL);
        cal.setTime(ultimaResposta);
        int horaUltimaResposta = cal.get(Calendar.HOUR_OF_DAY);

        // Hora atual também em horário de Brasília
        Calendar agora = Calendar.getInstance(TZ_BRASIL);
        int horaAgora = agora.get(Calendar.HOUR_OF_DAY);

        boolean foiMadrugada = horaUltimaResposta < 8;  // respondeu entre 00h e 07h59
        boolean eNoiteReal   = horaAgora >= 20;          // agora é 20h ou mais tarde

        if (foiMadrugada && eNoiteReal) {
            // Respondeu na madrugada e agora é noite real → libera nova resposta
            Logger.getLogger(EmaFacadeREST.class.getName()).log(Level.INFO,
                "Utilizador ID={0}: resposta anterior era madrugada ({1}h) — noite real liberada",
                new Object[]{ userId, horaUltimaResposta });
            return buildCheckResponse(false);
        }

        // Já respondeu na noite real → bloqueado
        return buildCheckResponse(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitário
    // ─────────────────────────────────────────────────────────────────────────

    private Response buildCheckResponse(boolean jaRespondeu) {
        return Response.ok("{\"respondeuNestePeriodo\": " + jaRespondeu + "}").build();
    }
}