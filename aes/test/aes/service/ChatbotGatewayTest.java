package aes.service;

import aes.model.Chat;
import aes.model.ChatbotInteraction;
import aes.model.Message;
import aes.model.User;
import aes.utility.ChatbotClient;
import aes.utility.ChatbotPolicy;
import aes.utility.SecurityContextHelper;
import com.sun.net.httpserver.HttpServer;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;

public class ChatbotGatewayTest {
    private static final String TOKEN = "synthetic-service-token-for-tests-only-123456";
    private static int checks;
    private static long nextActor = 100;

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    public static class Naming implements InitialContextFactory {
        @Override public Context getInitialContext(Hashtable<?, ?> environment) {
            return (Context) Proxy.newProxyInstance(Context.class.getClassLoader(), new Class<?>[]{Context.class},
                    (proxy, method, args) -> {
                        if ("lookup".equals(method.getName())) return Proxy.newProxyInstance(
                                UserTransaction.class.getClassLoader(), new Class<?>[]{UserTransaction.class},
                                (p, m, a) -> "getStatus".equals(m.getName()) ? 6 : null);
                        return null;
                    });
        }
    }

    private static class Resource extends ChatbotInteractionFacadeREST {
        final ChatbotClient client;
        Resource(ChatbotClient client) { this.client = client; }
        @Override protected ChatbotClient createChatbotClient() { return client; }
    }

    private static class Fixture {
        final User consultant = new User();
        final User patient = new User();
        final Chat chat = new Chat();
        final Message message = new Message();
        final Resource resource;
        final List<ChatbotInteraction> saved = new ArrayList<>();

        Fixture(ChatbotClient client) throws Exception {
            consultant.setId(nextActor++);
            consultant.setEmail("synthetic@example.invalid");
            consultant.setConsultant(true);
            consultant.setUse_chatbot(true);
            patient.setId(7L);
            patient.setRelatedConsultant(consultant);
            chat.setId(8L);
            chat.setUser(patient);
            message.setId(9L);
            message.setChat(chat);
            message.setIdFrom("7");
            message.setContent("mensagem sintética");
            resource = new Resource(client);
            SecurityContextHelper helper = new SecurityContextHelper() {
                @Override public Response requireAuthenticatedUser() { return null; }
                @Override public User getLoggedUser() { return consultant; }
            };
            EntityManager em = (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
                    new Class<?>[]{EntityManager.class}, (proxy, method, args) -> {
                        if ("find".equals(method.getName())) return Long.valueOf(9).equals(args[1]) ? message : null;
                        if ("persist".equals(method.getName())) {
                            ChatbotInteraction interaction = (ChatbotInteraction) args[0];
                            interaction.setId(99L);
                            saved.add(interaction);
                            return null;
                        }
                        if ("createQuery".equals(method.getName())) {
                            check(((String) args[0]).contains("m.chat.id = :chatId"), "Context query must be partitioned by chat");
                            return Proxy.newProxyInstance(TypedQuery.class.getClassLoader(), new Class<?>[]{TypedQuery.class},
                                    (p, m, a) -> {
                                        if ("setParameter".equals(m.getName())) {
                                            if ("chatId".equals(a[0])) check(Long.valueOf(8).equals(a[1]), "Chat ID must come from the stored message");
                                            if ("messageId".equals(a[0])) check(Long.valueOf(9).equals(a[1]), "Message cutoff must be bound");
                                            return p;
                                        }
                                        if ("setMaxResults".equals(m.getName())) { check(Integer.valueOf(10).equals(a[0]), "Context limit must be ten"); return p; }
                                        if ("getResultList".equals(m.getName())) return new ArrayList<>(Arrays.asList(message));
                                        throw new UnsupportedOperationException(m.getName());
                                    });
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            for (String name : new String[]{"em", "securityHelper"}) {
                Field field = ChatbotInteractionFacadeREST.class.getDeclaredField(name);
                field.setAccessible(true);
                field.set(resource, "em".equals(name) ? em : helper);
            }
        }

        Response generate() {
            ChatbotInteractionFacadeREST.SuggestionRequest request = new ChatbotInteractionFacadeREST.SuggestionRequest();
            request.messagePacienteId = 9L;
            return resource.generate(request);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, Naming.class.getName());
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger status = new AtomicInteger(200);
        String[] returned = {"{\"respostas\":[\"Sugestão sintética\"]}"};
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/generate", exchange -> {
            calls.incrementAndGet();
            check(("Bearer " + TOKEN).equals(exchange.getRequestHeaders().getFirst("Authorization")), "Service credential must be used");
            String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
            check(body.contains("paciente: mensagem sintética"), "Only server-selected context must be sent");
            if (status.get() == 302) exchange.getResponseHeaders().set("Location", "/must-not-follow");
            byte[] bytes = returned[0].getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.createContext("/must-not-follow", exchange -> { throw new AssertionError("Redirect must never receive the token"); });
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        try {
            for (String invalid : new String[]{base, "ftp://example.invalid", "https://user:pass@example.invalid",
                    "https://example.invalid?secret=1", "https://example.invalid#fragment"}) {
                try { new ChatbotClient(invalid, TOKEN, false); throw new AssertionError("Unsafe config accepted"); }
                catch (ChatbotClient.Failure expected) { check(expected.status == 503, "Bad config must fail closed"); }
            }
            ChatbotClient client = new ChatbotClient(base, TOKEN, true);
            Fixture valid = new Fixture(client);
            Response response = valid.generate();
            check(response.getStatus() == 200, "Authorized generation must succeed");
            check(valid.saved.size() == 1 && valid.saved.get(0).getResponse1() != null, "Backend must persist suggestions");
            check(Long.valueOf(8).equals(((Map<?, ?>) response.getEntity()).get("chatId")), "Response must identify its chat");
            check("no-store".equals(response.getHeaderString("Cache-Control")), "Conversations must not be HTTP-cached");

            int before = calls.get();
            Fixture forbidden = new Fixture(client);
            forbidden.patient.setRelatedConsultant(new User());
            check(forbidden.generate().getStatus() == 403, "Different consultant must be denied");
            forbidden.patient.setRelatedConsultant(forbidden.consultant);
            forbidden.consultant.setUse_chatbot(false);
            check(forbidden.generate().getStatus() == 403, "Disabled chatbot must be denied");
            forbidden.consultant.setUse_chatbot(true);
            forbidden.consultant.setConsultant(false);
            check(forbidden.generate().getStatus() == 403, "Patient cannot request consultant suggestions");
            check(calls.get() == before && forbidden.saved.isEmpty(), "Denied requests must not call the API or persist suggestions");

            Message otherChat = new Message();
            otherChat.setChat(new Chat(77L));
            try {
                ChatbotPolicy.buildContext(valid.consultant, valid.message, Arrays.asList(otherChat));
                throw new AssertionError("Cross-chat context accepted");
            } catch (IllegalArgumentException expected) { checks++; }

            for (int upstream : new int[]{302, 401, 429, 500}) {
                status.set(upstream);
                Response failed = new Fixture(client).generate();
                check(failed.getStatus() == (upstream == 429 ? 429 : 502), "Upstream failures/redirects must be rejected");
            }
            status.set(200);
            returned[0] = "{\"respostas\":[null]}";
            Fixture malformed = new Fixture(client);
            check(malformed.generate().getStatus() == 502 && malformed.saved.isEmpty(), "Invalid suggestions must not be persisted");
        } finally {
            server.stop(0);
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        }
        System.out.println("Chatbot gateway: " + checks + " checks passed");
    }

    private static byte[] readAll(java.io.InputStream input) throws java.io.IOException {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
        return bytes.toByteArray();
    }
}
