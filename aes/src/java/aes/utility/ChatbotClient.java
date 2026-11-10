package aes.utility;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatbotClient {
    public static class Failure extends Exception {
        public final int status;
        public final String code;
        public Failure(int status, String code) { super(code); this.status = status; this.code = code; }
    }

    private final URL endpoint;
    private final String serviceToken;

    public ChatbotClient(String baseUrl, String serviceToken, boolean allowHttp) throws Failure {
        try {
            URI uri = new URI(baseUrl);
            boolean https = "https".equals(uri.getScheme());
            boolean http = "http".equals(uri.getScheme());
            if ((!https && !(http && allowHttp)) || uri.getHost() == null || uri.getUserInfo() != null ||
                    uri.getQuery() != null || uri.getFragment() != null || uri.getPort() == 0 || uri.getPort() > 65535 ||
                    serviceToken == null || serviceToken.length() < 32 ||
                    serviceToken.chars().anyMatch(c -> c <= 32 || c >= 127)) {
                throw new IllegalArgumentException();
            }
            this.endpoint = new URL(baseUrl.replaceAll("/+$", "") + "/v1/generate");
            this.serviceToken = serviceToken;
        } catch (Exception e) {
            throw new Failure(503, "CHATBOT_NOT_CONFIGURED");
        }
    }

    private static String setting(String env, String property) {
        String value = System.getenv(env);
        return value == null || value.trim().isEmpty() ? System.getProperty(property, "") : value;
    }

    public static ChatbotClient fromEnvironment() throws Failure {
        return new ChatbotClient(setting("CHATBOT_BASE_URL", "aes.chatbot.baseUrl"),
                setting("CHATBOT_SERVICE_TOKEN", "aes.chatbot.serviceToken"),
                "true".equalsIgnoreCase(setting("CHATBOT_ALLOW_HTTP", "aes.chatbot.allowHttp")));
    }

    public List<String> generate(String conversation) throws Failure {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) endpoint.openConnection();
            connection.setInstanceFollowRedirects(false); // Never forward the service token to another destination.
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(90000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + serviceToken);
            byte[] body = new Gson().toJson(Collections.singletonMap("conversation", conversation)).getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream output = connection.getOutputStream()) { output.write(body); }
            int status = connection.getResponseCode();
            if (status == 429) throw new Failure(429, "CHATBOT_BUSY");
            if (status != 200) throw new Failure(502, "CHATBOT_UPSTREAM_FAILED");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (bytes.size() + count > 65536) throw new Failure(502, "CHATBOT_RESPONSE_TOO_LARGE");
                    bytes.write(buffer, 0, count);
                }
            }
            JsonObject response = new Gson().fromJson(new String(bytes.toByteArray(), StandardCharsets.UTF_8), JsonObject.class);
            JsonArray values = response.getAsJsonArray("respostas");
            if (values == null || values.size() < 1 || values.size() > 3) throw new IllegalArgumentException();
            List<String> suggestions = new ArrayList<>();
            for (JsonElement value : values) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new IllegalArgumentException();
                String text = value.getAsString();
                if (text.trim().isEmpty() || text.length() > 4000) throw new IllegalArgumentException();
                suggestions.add(text);
            }
            return suggestions;
        } catch (SocketTimeoutException e) {
            throw new Failure(504, "CHATBOT_TIMEOUT");
        } catch (IOException e) {
            throw new Failure(502, "CHATBOT_UNAVAILABLE");
        } catch (RuntimeException e) {
            throw new Failure(502, "INVALID_CHATBOT_RESPONSE");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
