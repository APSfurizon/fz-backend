package net.furizon.backend.infrastructure.localization.model;
import lombok.Getter;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nullable;

@Getter
public class TranslatableValue {
    private String key;
    private Object[] params;

    private TranslatableValue(String key, Object... params) {
        this.key = key;
        this.params = params;
    }

    public static TranslatableValue ofEmail(@PropertyKey(resourceBundle = "email.email") String key,
                                            @Nullable Object... params) {
        return new TranslatableValue(key, params);
    }

    public static TranslatableValue ofError(@PropertyKey(resourceBundle = "errors.errors") String key,
                                            @Nullable Object... params) {
        return new TranslatableValue(key, params);
    }

    public static TranslatableValue ofMessage(@PropertyKey(resourceBundle = "messages.messages") String key,
                                            @Nullable Object... params) {
        return new TranslatableValue(key, params);
    }
}
