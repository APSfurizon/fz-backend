package net.furizon.backend.infrastructure.security.session.action.createSession;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface CreateSessionAction {
    @NotNull
    UUID invoke(
        long userId,
        @NotNull String clientIp,
        @Nullable String userAgent
    );
}
