package net.furizon.backend.infrastructure.security.session.action.updateSession;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface UpdateSessionAction {
    void invoke(@NotNull UUID sessionId, @NotNull String clientIp);
}
