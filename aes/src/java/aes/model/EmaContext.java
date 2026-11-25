package aes.model;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Stable category codes shared with the mobile app. Never reuse an existing ID. */
public final class EmaContext {
    private static final Map<String, Integer> COMPANY = new HashMap<>();
    private static final Map<String, Integer> LOCATION = new HashMap<>();

    static {
        register(COMPANY, 1, "Sozinho", "Alone");
        register(COMPANY, 2, "Amigos", "Friends");
        register(COMPANY, 3, "Parceiro(a)", "Partner");
        register(COMPANY, 4, "Família", "Family");
        register(COMPANY, 5, "Colegas de Trabalho", "Coworkers");
        register(COMPANY, 6, "Outros", "Others");
        register(LOCATION, 1, "Casa", "Home");
        register(LOCATION, 2, "Trabalho/Escola", "Work/School");
        register(LOCATION, 3, "Bar/Restaurante/Balada", "Bar/Restaurant/Club");
        register(LOCATION, 4, "Festa (Casa de terceiros)", "Party (Someone else's house)");
        register(LOCATION, 5, "Trânsito/Rua", "Transit/Street");
        register(LOCATION, 6, "Outro", "Other");
    }

    private EmaContext() { }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static void register(Map<String, Integer> codes, int id, String... labels) {
        for (String label : labels) codes.put(normalize(label), id);
    }

    public static boolean hasIds(EmaAnswer answer) {
        return answer.getCompanyId() != null || answer.getCurrentLocationId() != null;
    }

    private static boolean validText(String value) {
        return value != null && !value.trim().isEmpty() && value.length() <= 100;
    }

    public static boolean isValid(EmaAnswer answer) {
        if (answer == null) return false;
        if (hasIds(answer)) {
            // Reject incomplete codes and ambiguous mixtures of IDs and texts.
            return COMPANY.containsValue(answer.getCompanyId()) && LOCATION.containsValue(answer.getCurrentLocationId()) &&
                    answer.getCompany() == null && answer.getCurrentLocation() == null;
        }
        return validText(answer.getCompany()) && validText(answer.getCurrentLocation());
    }

    public static EmaAnswer forStorage(EmaAnswer request) {
        EmaAnswer stored = new EmaAnswer(request);
        if (!hasIds(request)) {
            stored.setCompanyId(request.getCompany() == null ? null : COMPANY.get(normalize(request.getCompany())));
            stored.setCurrentLocationId(request.getCurrentLocation() == null ? null : LOCATION.get(normalize(request.getCurrentLocation())));
        }
        // Unknown legacy labels keep their exact original value; never guess "Other".
        if (stored.getCompanyId() != null) stored.setCompany(null);
        if (stored.getCurrentLocationId() != null) stored.setCurrentLocation(null);
        return stored;
    }
}
