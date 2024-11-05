package net.furizon.backend.infrastructure.security.session.action.deleteSession;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface DeleteSessionAction {
    void invoke(@NotNull final UUID sessionId);
}
