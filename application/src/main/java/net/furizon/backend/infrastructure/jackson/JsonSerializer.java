package net.furizon.backend.infrastructure.jackson;

import org.jetbrains.annotations.NotNull;
import org.jooq.JSON;

public interface JsonSerializer {
    @NotNull
    String serializeAsString(Object object);

    @NotNull
    JSON serializeAsJson(Object object);
}
