package net.furizon.backend.infrastructure.localization;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslationService {
    @NotNull
    private final MessageSource messageSource;

    public String translate(String key, Locale locale, @Nullable Object... params) {
        try {
            return messageSource.getMessage(key, params, locale);
        } catch (Throwable e) {
            return "%s (%s)".formatted(key, Arrays.stream(params != null ? params : new Object[] {})
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
        }

    }

    public String translate(String key, @Nullable Object... params) {
        Locale locale = LocaleContextHolder.getLocale();
        return translate(key, locale, params);
    }

    public String translate(String key) {
        return translate(key, (Object[]) null);
    }

}
