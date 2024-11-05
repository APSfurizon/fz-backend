package net.furizon.backend.infrastructure.security.session.action.clearNewestUserSessions;

public interface ClearNewestUserSessionsAction {
    void invoke(long userId);
}
