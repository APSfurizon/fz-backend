package net.furizon.backend.infrastructure.pretix.autocart;

import org.jetbrains.annotations.NotNull;

public record AutocartAction<T>(@NotNull String targetId, @NotNull T value) {
    public AutocartActionType getType() {
        return value instanceof Boolean ? AutocartActionType.BOOL : AutocartActionType.VALUE;
    }
}
