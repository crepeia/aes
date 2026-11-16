package aes.utility;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/** Offline checks: no application server, database or email delivery. */
public final class MessageResourcesTest {
    private static final List<String> LANGUAGES = Arrays.asList("pt", "en", "es");
    private static final Pattern COMMENTS = Pattern.compile(
            "\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/");
    private static final Pattern JAVA_KEY = Pattern.compile("\\bgetString\\(\\s*\"([^\"\\r\\n]+)\"\\s*[,)]");
    private static final Pattern VIEW_KEY = Pattern.compile(
            "messages\\[['\"]([^'\"]+)['\"]\\]|messages\\.([a-zA-Z_][a-zA-Z_0-9]*)");
    private static final Pattern EMAIL_KEY = Pattern.compile("contact\\.set(?:Subject|Content)\\(\"([^\"]+)\"\\s*\\)");
    private static int references;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static String uncomment(String source) {
        Matcher matcher = COMMENTS.matcher(source);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    token.startsWith("//") || token.startsWith("/*") ? " " : token));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static void checkKey(String key, String location) {
        for (String language : LANGUAGES) {
            ResourceBundle bundle = MessageResources.getBundle(language);
            check(bundle.containsKey(key), location + ": missing " + key + " in " + language);
        }
        references++;
    }

    private static void checkDictionaries(Path project) throws IOException {
        Set<Object> expected = null;
        for (String language : LANGUAGES) {
            Properties properties = new Properties() {
                @Override
                public synchronized Object put(Object key, Object value) {
                    check(!containsKey(key), "Duplicate translation: " + key);
                    return super.put(key, value);
                }
            };
            Path file = project.resolve("src/java/aes/utility/messages_" + language + ".properties");
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            if (expected == null) expected = new HashSet<>(properties.keySet());
            check(expected.equals(properties.keySet()), "Different key sets in " + language);
            for (Object rawKey : properties.keySet()) {
                String key = (String) rawKey;
                // These two unused/optional labels are deliberately empty in the original sheets.
                if (key.equals("doseChatStepControl06") || key.equals("preparando.bene.btn.title")) continue;
                check(!properties.getProperty(key).trim().isEmpty(), "Empty translation: " + language + ":" + key);
            }
        }
    }

    private static void checkSources(Path directory, boolean java) throws IOException {
        List<Path> files;
        try (Stream<Path> paths = Files.walk(directory)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(p -> java ? p.toString().endsWith(".java") : p.toString().endsWith(".xhtml"))
                    .collect(Collectors.toList());
        }
        for (Path file : files) {
            String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            source = java ? uncomment(source) : source.replaceAll("(?s)<!--.*?-->", "");
            if (!java) {
                Matcher validation = Pattern.compile("(?:requiredMessage|validatorMessage|converterMessage)=\"([^\"]*)\"").matcher(source);
                while (validation.find()) {
                    check(validation.group(1).contains("#{"), "Untranslated or empty validation message in " + file);
                }
            }
            Matcher matcher = (java ? JAVA_KEY : VIEW_KEY).matcher(source);
            while (matcher.find()) {
                String key = matcher.group(1);
                if (key == null) key = matcher.group(2);
                checkKey(key, file.toString());
            }
            if (java) {
                check(!Pattern.compile("\\bcontact\\.getSubject\\(\\)\\.(?:contains|equals|startsWith)\\(").matcher(source).find(),
                        "Email classification must normalize legacy identifiers in " + file);
                matcher = EMAIL_KEY.matcher(source);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    // sendTestEmail sends these literals directly, without a translation lookup.
                    if (!key.equals("teste de envio")) checkKey(key, file.toString());
                }
            }
        }
    }

    private static void checkDynamicKeys() {
        for (int month = 1; month <= 12; month++) checkKey("month." + month, "months");
        for (int tip = 1; tip <= 67; tip++) checkKey("tip" + tip, "tips");
        for (int week : new int[]{1, 2, 3, 4, 8, 12, 24, 48}) {
            checkKey("week" + week, "weekly emails");
            checkKey("week" + week + "Subj", "weekly emails");
        }
        for (String prefix : new String[]{"challengePrefix", "challengeBaseValue", "challengeModifier", "challengeType"}) {
            checkKey(prefix + "1", "challenge editor");
        }
        for (String language : LANGUAGES) {
            ResourceBundle bundle = MessageResources.getBundle(language);
            for (String topic : bundle.getString("readyText.topics").split(",")) {
                String base = "readyText." + topic.trim();
                checkKey(base + ".title", "ready texts");
                int count = Integer.parseInt(bundle.getString(base + ".count"));
                for (int i = 1; i <= count; i++) checkKey(base + "." + i, "ready texts");
            }
        }
    }

    private static void checkLegacyEmails() {
        String[][] pairs = {
            {"passwordrecovery_subj", "passwordrecoverySubj"}, {"deleteaccount_subj", "deleteaccountSubj"},
            {"sendContactRequest_subj", "sendContactRequestSubj"}, {"plan_subj", "planSubj"},
            {"record_subj", "recordSubj"}, {"start_subj", "startSubj"}, {"start_msg", "startMsg"},
            {"welcome_subj", "welcomeSubj"}, {"annualscreening_subj", "annualscreeningSubj"},
            {"tips_subj", "tipsSubj"},
            {"email.msg.subject.diary_started_subj", "emailMsgSubjectDiaryStartedSubj"},
            {"email.msg.subject.diary_started", "emailMsgSubjectDiaryStarted"},
            {"progress_persistchallenges_reduce_subj", "progressPersistchallengesReduceSubj"},
            {"progress_persistchallenges_reduce", "progressPersistchallengesReduce"},
            {"progress_persistchallenges_quit_subj", "progressPersistchallengesQuitSubj"},
            {"progress_persistchallenges_quit", "progressPersistchallengesQuit"},
            {"progress_keepingresult_reduce_subj", "progressKeepingresultReduceSubj"},
            {"progress_keepingresult_reduce", "progressKeepingresultReduce"},
            {"progress_keepingresult_quit_subj", "progressKeepingresultQuitSubj"},
            {"progress_keepingresult_quit", "progressKeepingresultQuit"}
        };
        for (String language : LANGUAGES) {
            for (String[] pair : pairs) {
                check(MessageResources.canonicalKey(pair[0]).equals(pair[1]), "Legacy email classification: " + pair[0]);
                check(MessageResources.canonicalKey(pair[1]).equals(pair[1]), "Current email classification: " + pair[1]);
                check(MessageResources.getString(pair[0], language).equals(
                        MessageResources.getString(pair[1], language)), "Legacy email: " + pair[0]);
            }
            for (int tip = 1; tip <= 67; tip++) {
                check(MessageResources.getString("tips" + tip, language).equals(
                        MessageResources.getString("tip" + tip, language)), "Legacy tip " + tip);
            }
            for (int week : new int[]{1, 2, 3, 4, 8, 12, 24, 48}) {
                for (boolean subject : new boolean[]{false, true}) {
                    check(MessageResources.getString("week_" + week + (subject ? "_subj" : ""), language).equals(
                            MessageResources.getString("week" + week + (subject ? "Subj" : ""), language)), "Legacy week " + week);
                }
            }
        }
    }

    private static void checkLocales() throws Exception {
        for (String tag : new String[]{"pt", "pt-BR", "pt_PT", "en-US", "en_GB", "es-AR"}) {
            check(MessageResources.getBundle(tag).getLocale().getLanguage().equals(tag.substring(0, 2)), "Locale " + tag);
            check(MessageResources.getBundle(new Locale(tag)).getLocale().getLanguage().equals(tag.substring(0, 2)), "Legacy locale " + tag);
        }
        for (String tag : new String[]{null, "", "fr-FR", "invalid"}) {
            check(MessageResources.getBundle(tag).getLocale().getLanguage().equals("pt"), "Default locale " + tag);
        }
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            check(MessageResources.getBundle("unsupported").getLocale().getLanguage().equals("pt"), "JVM locale leaked");
        } finally {
            Locale.setDefault(original);
        }
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<Callable<Void>> lookups = new ArrayList<>();
            for (String language : LANGUAGES) {
                final String expected = MessageResources.getString("emailUsed", language);
                lookups.add(() -> {
                    for (int i = 0; i < 1000; i++) {
                        check(expected.equals(MessageResources.getString("emailUsed", language)), "Mixed languages");
                    }
                    return null;
                });
            }
            for (Future<Void> future : executor.invokeAll(lookups)) future.get();
        } finally {
            executor.shutdownNow();
        }
        try {
            MessageResources.getString("missingTranslationForRegressionTest", "pt");
            throw new AssertionError("An unknown key must fail visibly");
        } catch (MissingResourceException expected) {
            // A fallback must never hide a spelling mistake in a key.
        }
    }

    private static void checkFacesConfig(Path project) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder().parse(project.resolve("web/WEB-INF/faces-config.xml").toFile());
        check(document.getElementsByTagName("message-bundle").item(0).getTextContent().trim().equals("aes.utility.messages"), "JSF message bundle");
        check(document.getElementsByTagName("message-bundle").item(0).getChildNodes().getLength() == 1, "message-bundle must contain a name, not XML elements");
        for (String key : new String[]{"javax.faces.component.UIInput.REQUIRED", "javax.faces.validator.LengthValidator.MINIMUM",
            "javax.faces.validator.LengthValidator.MAXIMUM", "primefaces.captcha.INVALID", "primefaces.captcha.INVALID_detail",
            "primefaces.password.INVALID_MATCH"}) checkKey(key, "framework messages");
    }

    public static void main(String[] args) throws Exception {
        Path project = Paths.get(args[0]);
        checkDictionaries(project);
        checkSources(project.resolve("src/java"), true);
        checkSources(project.resolve("web"), false);
        checkDynamicKeys();
        checkLegacyEmails();
        checkLocales();
        checkFacesConfig(project);
        System.out.println("OK: " + references + " translation references in pt/en/es; legacy emails, locale isolation and JSF configuration.");
    }
}
