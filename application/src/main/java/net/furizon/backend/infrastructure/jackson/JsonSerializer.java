package net.furizon.backend.infrastructure.jackson;

import org.jetbrains.annotations.NotNull;

public interface JsonSerializer {
    @NotNull
    String serialize(Object object);
}
