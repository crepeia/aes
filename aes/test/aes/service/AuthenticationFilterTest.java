package aes.service;

import aes.model.AuthenticationToken;
import aes.model.User;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

// Runs without a server or database. Exercises the actual JAX-RS filter.
public class AuthenticationFilterTest {
    private static final String TOKEN = "synthetic-session-token-not-for-production";
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static class Fixture {
        Response aborted;
        SecurityContext security;
        int queries;
        final Map<String, Object> properties = new HashMap<>();
        final Map<String, Object> parameters = new HashMap<>();

        Fixture(String header, AuthenticationToken authentication, RuntimeException failure) throws Exception {
            Query query = (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[]{Query.class},
                    (proxy, method, args) -> {
                        if ("setParameter".equals(method.getName())) {
                            parameters.put((String) args[0], args[1]);
                            return proxy;
                        }
                        if ("getSingleResult".equals(method.getName())) {
                            if (failure != null) throw failure;
                            return authentication;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            EntityManager em = (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
                    new Class<?>[]{EntityManager.class}, (proxy, method, args) -> {
                        if ("createQuery".equals(method.getName())) {
                            queries++;
                            check(((String) args[0]).contains("dateCreated >= :validDate"), "Expiry must be queried");
                            return query;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            ContainerRequestContext request = (ContainerRequestContext) Proxy.newProxyInstance(
                    ContainerRequestContext.class.getClassLoader(), new Class<?>[]{ContainerRequestContext.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getHeaderString": return header;
                            case "getSecurityContext": return security;
                            case "setSecurityContext": security = (SecurityContext) args[0]; return null;
                            case "setProperty": properties.put((String) args[0], args[1]); return null;
                            case "abortWith": aborted = (Response) args[0]; return null;
                            default: throw new UnsupportedOperationException(method.getName());
                        }
                    });
            AuthenticationFilter filter = new AuthenticationFilter();
            Field field = AuthenticationFilter.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(filter, em);
            filter.filter(request);
        }
    }

    public static void main(String[] args) throws Exception {
        final StringBuilder logs = new StringBuilder();
        Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
        Handler handler = new Handler() {
            @Override public void publish(LogRecord record) {
                logs.append(record.getMessage());
                if (record.getParameters() != null) {
                    for (Object value : record.getParameters()) logs.append(value);
                }
                if (record.getThrown() != null) logs.append(record.getThrown().getMessage());
            }
            @Override public void flush() {}
            @Override public void close() {}
        };
        logger.addHandler(handler);
        try {
            for (String header : new String[]{null, "Basic synthetic", "Bearer ", "Bearer"}) {
                Fixture f = new Fixture(header, null, null);
                check(f.aborted != null && f.aborted.getStatus() == 401, "Missing/malformed token must be rejected");
                check(f.queries == 0 && f.security == null, "Rejected headers must not reach database or create a principal");
            }

            Fixture invalid = new Fixture("Bearer " + TOKEN, null, new NoResultException(TOKEN));
            check(invalid.aborted.getStatus() == 401, "Unknown or expired token must be rejected");
            check(invalid.security == null && invalid.properties.isEmpty(), "Invalid token must not enter request context");
            check(invalid.aborted.getHeaderString("WWW-Authenticate").startsWith("Bearer "), "401 needs Bearer challenge");

            Fixture unavailable = new Fixture("Bearer " + TOKEN, null, new IllegalStateException(TOKEN));
            check(unavailable.aborted.getStatus() == 503, "Database failure must fail closed");
            check(unavailable.security == null, "Database failure must not create a principal");

            User user = new User();
            user.setId(42L);
            user.setEmail("synthetic@example.invalid");
            user.setConsultant(true);
            AuthenticationToken authentication = new AuthenticationToken();
            authentication.setUser(user);
            Fixture valid = new Fixture("bearer " + TOKEN, authentication, null);
            check(valid.aborted == null, "Valid token must be accepted");
            check("42".equals(valid.security.getUserPrincipal().getName()), "Principal must be the validated user");
            check(valid.queries == 1, "Principal must reuse the validated token record");
            check(valid.parameters.get("validDate") instanceof Date, "Expiry cutoff must be bound");
            check(valid.security.isUserInRole("consultant") && !valid.security.isUserInRole("admin"), "Roles must reflect actual permissions");
            check(!valid.security.isUserInRole("unknown") && !valid.security.isUserInRole(null), "Unknown roles must be denied");
            check(TOKEN.equals(valid.properties.get("AUTH_TOKEN")), "Existing helpers still need the validated token");
            check(!valid.security.isSecure(), "Filter must not label HTTP as HTTPS");

            authentication.setUser(null);
            Fixture anonymous = new Fixture("Bearer " + TOKEN, authentication, null);
            check(anonymous.aborted == null && anonymous.security.getUserPrincipal() == null,
                    "Unbound anonymous tokens must preserve their existing no-principal behavior");
            check(!anonymous.security.isUserInRole("admin"), "Unbound token cannot grant a role");
            check(!logs.toString().contains(TOKEN), "Logs must not contain token or exception message carrying it");
        } finally {
            logger.removeHandler(handler);
        }
        System.out.println("AuthenticationFilter: " + checks + " checks passed");
    }
}
