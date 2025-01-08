package net.furizon.backend.infrastructure.email;

import org.jetbrains.annotations.NotNull;

public record MailVarPair(
        @NotNull EmailVars var,
        @NotNull String value
) {
    public static MailVarPair of(@NotNull EmailVars var,
                                 @NotNull String value) {
        return new MailVarPair(var, value);
    }
}
