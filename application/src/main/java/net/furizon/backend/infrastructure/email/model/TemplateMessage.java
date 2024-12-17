package net.furizon.backend.infrastructure.email.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


@Data
public class TemplateMessage {
    @NotNull
    private final String template;

    @NotNull
    private final Map<String, Object> params;

    public TemplateMessage addParam(@NotNull String key, @NotNull Object value) {
        params.put(key, value.toString());
        return this;
    }

    public static TemplateMessage of(@NotNull String templatePath, @NotNull Map<String, Object> params) {
        return new TemplateMessage(templatePath, params);
    }

    public static TemplateMessage of(@NotNull String templatePath) {
        return of(templatePath, new HashMap<>());
    }
}
