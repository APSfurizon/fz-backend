package net.furizon.backend.feature.authentication.action.createAuthentication;

import org.jetbrains.annotations.NotNull;

public interface CreateAuthenticationAction {
    void invoke(
        long userId,
        @NotNull String email,
        @NotNull String password
    );
}
