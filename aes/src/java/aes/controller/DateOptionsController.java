package aes.controller;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author luansb
 */
@Named("dateOptions")
@ApplicationScoped
public class DateOptionsController {
    private final Map<String, String> dias = new LinkedHashMap<>();
    private final Map<String, String> anos = new LinkedHashMap<>();

    @PostConstruct
    public void init() {

        for (int i = 1; i <= 31; i++) {
            dias.put(String.valueOf(i), String.valueOf(i));
        }

        int lastYear = GregorianCalendar.getInstance()
                .get(Calendar.YEAR) - 1;

        for (int i = lastYear; i > lastYear - 100; i--) {
            anos.put(String.valueOf(i), String.valueOf(i));
        }
    }

    public Map<String, String> getDias() {
        return dias;
    }

    public Map<String, String> getAnos() {
        return anos;
    }
}