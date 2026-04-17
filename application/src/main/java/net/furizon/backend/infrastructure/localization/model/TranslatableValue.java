package net.furizon.backend.infrastructure.localization.model;
import lombok.Getter;
import net.furizon.backend.infrastructure.localization.TranslationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nullable;
import java.util.Locale;

@Getter
public class TranslatableValue {
    private String key;
    private Object[] params;

    private TranslatableValue(String key, Object... params) {
        this.key = key;
        this.params = params;
    }

    public @NotNull String localizeError(@NotNull TranslationService translationService) {
        return translationService.error(key, params);
    }

    public @NotNull String localizeEmail(@NotNull TranslationService translationService) {
        return translationService.error(key, params);
    }

    public @NotNull String translateFallback(@NotNull TranslationService translationService) {
        return translationService.translateFallback(key, key, params);
    }

    public @NotNull String translateFallback(@NotNull Locale locale, @NotNull TranslationService translationService) {
        return translationService.translateFallback(key, key, locale, params);
    }

    public static TranslatableValue ofEmail(@PropertyKey(resourceBundle = "email.email") String key,
                                            @Nullable Object... params) {
        return new TranslatableValue(key, params);
    }

    public static TranslatableValue ofError(@PropertyKey(resourceBundle = "errors.errors") String key,
                                            @Nullable Object... params) {
        return new TranslatableValue(key, params);
    }
}
