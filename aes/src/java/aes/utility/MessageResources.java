package aes.utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Translation lookup, including identifiers saved by older email schedulers. */
public final class MessageResources {

    private static final String BUNDLE_NAME = "aes.utility.messages";
    private static final Pattern WEEK_KEY = Pattern.compile("week_(\\d+)(_subj)?");
    private static final Pattern TIP_KEY = Pattern.compile("tips(\\d+)");
    private static final Map<String, String> EMAIL_ALIASES;

    static {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("passwordrecovery_subj", "passwordrecoverySubj");
        aliases.put("deleteaccount_subj", "deleteaccountSubj");
        aliases.put("sendContactRequest_subj", "sendContactRequestSubj");
        aliases.put("plan_subj", "planSubj");
        aliases.put("record_subj", "recordSubj");
        aliases.put("start_subj", "startSubj");
        aliases.put("start_msg", "startMsg");
        aliases.put("welcome_subj", "welcomeSubj");
        aliases.put("email.msg.subject.diary_started_subj", "emailMsgSubjectDiaryStartedSubj");
        aliases.put("email.msg.subject.diary_started", "emailMsgSubjectDiaryStarted");
        aliases.put("progress_persistchallenges_reduce_subj", "progressPersistchallengesReduceSubj");
        aliases.put("progress_persistchallenges_reduce", "progressPersistchallengesReduce");
        aliases.put("progress_persistchallenges_quit_subj", "progressPersistchallengesQuitSubj");
        aliases.put("progress_persistchallenges_quit", "progressPersistchallengesQuit");
        aliases.put("progress_keepingresult_reduce_subj", "progressKeepingresultReduceSubj");
        aliases.put("progress_keepingresult_reduce", "progressKeepingresultReduce");
        aliases.put("progress_keepingresult_quit_subj", "progressKeepingresultQuitSubj");
        aliases.put("progress_keepingresult_quit", "progressKeepingresultQuit");
        aliases.put("annualscreening_subj", "annualscreeningSubj");
        aliases.put("tips_subj", "tipsSubj");
        EMAIL_ALIASES = Collections.unmodifiableMap(aliases);
    }

    private MessageResources() {
    }

    public static String canonicalKey(String key) {
        Matcher week = WEEK_KEY.matcher(key);
        if (week.matches()) {
            return "week" + week.group(1) + (week.group(2) == null ? "" : "Subj");
        }
        Matcher tip = TIP_KEY.matcher(key);
        if (tip.matches()) {
            return "tip" + tip.group(1);
        }
        return EMAIL_ALIASES.getOrDefault(key, key);
    }

    public static Locale resolveLocale(String languageTag) {
        String language = languageTag == null ? "pt"
                : Locale.forLanguageTag(languageTag.trim().replace('_', '-')).getLanguage();
        if (!"pt".equals(language) && !"en".equals(language) && !"es".equals(language)) {
            language = "pt";
        }
        return new Locale(language);
    }

    public static ResourceBundle getBundle(String language) {
        return ResourceBundle.getBundle(BUNDLE_NAME, resolveLocale(language));
    }

    public static ResourceBundle getBundle(Locale locale) {
        return getBundle(locale == null ? null : locale.getLanguage());
    }

    public static String getString(String key, String language) {
        return getBundle(language).getString(canonicalKey(key));
    }

    public static String getString(String key, Locale locale) {
        return getBundle(locale).getString(canonicalKey(key));
    }
}
