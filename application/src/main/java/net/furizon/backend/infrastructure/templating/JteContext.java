package net.furizon.backend.infrastructure.templating;

import gg.jte.Content;
import org.jetbrains.annotations.PropertyKey;

public class JteContext {
    private static final ThreadLocal<JteLocalizer> context = new ThreadLocal<>();

    public static Content localize(@PropertyKey(resourceBundle = "email.email") String key) {
        return context.get().localize(key);
    }

    public static Content localize(@PropertyKey(resourceBundle = "email.email") String key, Object... params) {
        return context.get().localize(key, params);
    }

    public static void init(JteLocalizer localizer) {
        context.set(localizer);
    }

    public static void dispose() {
        context.remove();
    }
}
