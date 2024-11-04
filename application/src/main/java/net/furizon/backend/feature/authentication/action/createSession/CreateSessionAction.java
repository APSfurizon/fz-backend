package net.furizon.backend.feature.authentication.action.createSession;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface CreateSessionAction {
    UUID invoke(
        long userId,
        @NotNull String clientIp,
        @Nullable String userAgent
    );
}
