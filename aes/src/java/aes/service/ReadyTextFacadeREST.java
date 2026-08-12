package aes.service;

import aes.model.ReadyTextInteraction;
import aes.model.User;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author luansb
 */
@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("readytext")
public class ReadyTextFacadeREST extends AbstractFacade<ReadyTextInteraction> {
    private static final String DEFAULT_LANG = "pt";
    private static final String READY_TEXT_PREFIX = "readyText.";

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    @Inject
    private SecurityContextHelper securityHelper;

    public ReadyTextFacadeREST() {
        super(ReadyTextInteraction.class);
    }

    public static class ReadyTextItemDTO {
        public String tag;
        public String text;

        public ReadyTextItemDTO(String tag, String text) {
            this.tag = tag;
            this.text = text;
        }
    }

    public static class ReadyTextTopicDTO {
        public String topic;
        public String title;
        public List<ReadyTextItemDTO> items = new ArrayList<>();

        public ReadyTextTopicDTO(String topic, String title) {
            this.topic = topic;
            this.title = title;
        }
    }

    private String resolveLang(String langParam) {
        if (langParam == null) {
            return DEFAULT_LANG;
        }
        String lang = langParam.trim().toLowerCase();
        if (lang.equals("pt") || lang.equals("es") || lang.equals("en")) {
            return lang;
        }
        return DEFAULT_LANG;
    }

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON})
    public Response list(@QueryParam("lang") String langParam) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;

            String lang = resolveLang(langParam);
            String propertiesPath = this.getMessagesPath() + "_" + lang + ".properties";

            InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesPath);
            if (input == null) {
                Logger.getLogger(ReadyTextFacadeREST.class.getName())
                    .log(Level.SEVERE, "Properties file not found: {0}", propertiesPath);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
            }

            Properties properties = new Properties();
            try {
                properties.load(input);
            } finally {
                input.close();
            }

            List<ReadyTextTopicDTO> result = new ArrayList<>();

            String topicsCsv = properties.getProperty(READY_TEXT_PREFIX + "topics", "").trim();
            if (!topicsCsv.isEmpty()) {
                for (String rawTopic : topicsCsv.split(",")) {
                    String topic = rawTopic.trim();
                    if (topic.isEmpty()) continue;

                    String base = READY_TEXT_PREFIX + topic;
                    String title = properties.getProperty(base + ".title", topic);

                    int count = 0;
                    try {
                        count = Integer.parseInt(properties.getProperty(base + ".count", "0").trim());
                    } catch (NumberFormatException ignore) {
                        count = 0;
                    }

                    ReadyTextTopicDTO topicDTO = new ReadyTextTopicDTO(topic, title);
                    for (int i = 1; i <= count; i++) {
                        String key = base + "." + i;
                        String text = properties.getProperty(key);
                        if (text != null) {
                            topicDTO.items.add(new ReadyTextItemDTO(key, text));
                        }
                    }
                    result.add(topicDTO);
                }
            }

            Gson gson = new Gson();
            return Response.ok(gson.toJson(result)).build();
        } catch (IOException | RuntimeException ex) {
            Logger.getLogger(ReadyTextFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}