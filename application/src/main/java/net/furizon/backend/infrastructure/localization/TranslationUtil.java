package net.furizon.backend.infrastructure.localization;

import java.util.Locale;

public class TranslationUtil {
    public static final Locale DEFAULT_LOCALE = Locale.UK;

    public static Locale parseLocale(String locale) {
        final String[] slugs;
        if (locale.contains("_")) {
            slugs = locale.split("_");
        } else {
            slugs = locale.split("-");
        }

        return switch (slugs.length) {
            case 0 -> DEFAULT_LOCALE;
            case 1 -> Locale.of(slugs[0], slugs[0]);
            default -> Locale.of(slugs[0], slugs[1]);
        };
    }
}
