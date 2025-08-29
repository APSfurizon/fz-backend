package net.furizon.backend.infrastructure.localization;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.PropertyKey;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslationService {
    @NotNull
    private final MessageSource messageSource;

    public String translateFallback(String key, String fallback, Locale locale, @Nullable Object[] params) {
        try {
            return messageSource.getMessage(key, params, locale);
        } catch (Throwable e) {
            return fallback;
        }
    }

    public String translateFallback(String key, String fallback, @Nullable Object[] params) {
        return translateFallback(key, fallback, LocaleContextHolder.getLocale(), params);
    }

    private String translate(String key, Locale locale, @Nullable Object[] params) {
        try {
            return messageSource.getMessage(key, params, locale);
        } catch (Throwable e) {
            return "%s (%s)".formatted(key, Arrays.stream(params != null ? params : new Object[] {})
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
        }
    }

    private String translate(String key, @Nullable Object[] params) {
        Locale locale = LocaleContextHolder.getLocale();
        return translate(key, locale, params);
    }

    private String translate(String key) {
        return translate(key, null);
    }

    public String message(@PropertyKey(resourceBundle = "messages.messages") String key, Locale locale,
                        @Nullable Object[] params) {
        return translate(key, locale, params);
    }

    public String message(@PropertyKey(resourceBundle = "messages.messages") String key, @Nullable Object[] params) {
        return translate(key, params);
    }

    public String message(@PropertyKey(resourceBundle = "messages.messages") String key) {
        return translate(key);
    }

    public String error(@PropertyKey(resourceBundle = "errors.errors") String key, Locale locale,
                        @Nullable Object[] params) {
        return translate(key, locale, params);
    }

    public String error(@PropertyKey(resourceBundle = "errors.errors") String key, @Nullable Object[] params) {
        return translate(key, params);
    }

    public String error(@PropertyKey(resourceBundle = "errors.errors") String key) {
        return translate(key);
    }

    public String email(@PropertyKey(resourceBundle = "email.email") String key, Locale locale,
                          @Nullable Object[] params) {
        return translate(key, locale, params);
    }

    public String email(@PropertyKey(resourceBundle = "email.email") String key, @Nullable Object[] params) {
        return translate(key, params);
    }

    public String email(@PropertyKey(resourceBundle = "email.email") String key) {
        return translate(key);
    }

    public String getTranslationFromMap(Map<String, String> translationMap, Locale locale, boolean fallback) {
        if (translationMap.isEmpty()) {
            throw new IllegalArgumentException("Empty translation map");
        }

        return translationMap.getOrDefault(locale.getISO3Language(), fallback
                ? translationMap.getOrDefault("en", "?")
                : "?");
    }

    public String getTranslationFromMap(Map<String, String> translationMap, boolean fallback) {
        Locale locale = LocaleContextHolder.getLocale();
        return getTranslationFromMap(translationMap, locale, fallback);
    }

    public String getTranslationFromMap(Map<String, String> translationMap) {
        return getTranslationFromMap(translationMap, true);
    }

}
