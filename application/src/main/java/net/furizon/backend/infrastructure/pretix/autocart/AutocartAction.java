package net.furizon.backend.infrastructure.pretix.autocart;

import org.jetbrains.annotations.NotNull;

public record AutocartAction<T>(@NotNull String targetId, @NotNull T value, AutocartActionType type) {
    public static AutocartAction<String> withoutValue(@NotNull String targetId, AutocartActionType type) {
        return new AutocartAction<String>(targetId, "", type);
    }
}
