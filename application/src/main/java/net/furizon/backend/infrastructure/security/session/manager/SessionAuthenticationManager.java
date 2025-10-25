package net.furizon.backend.infrastructure.security.session.manager;

import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.infrastructure.security.session.Session;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface SessionAuthenticationManager {
    //Write
    boolean updateSession(@NotNull UUID sessionId, @NotNull String clientIp);
    boolean deleteSession(@NotNull UUID sessionId);
    @NotNull UUID createSession(long userId, @NotNull String clientIp, @Nullable String userAgent);

    void clearOldestSessions(long userId);
    void clearAllSession(long userId);

    //Read
    @NotNull List<Session> getUserSessions(long userId);
    int getUserSessionsCount(long userId);
    @Nullable Triple<Session, Authentication, Locale> findSessionWithAuthenticationById(UUID sessionId);

    @Nullable Authentication findAuthenticationByEmail(@NotNull String email);
    @Nullable Authentication findAuthenticationByUserId(long userId);

    /**
     * @return UUID the id which needs to be opened to confirm the email
     */
    UUID createAuthentication(long userId, @NotNull String email, @NotNull String password);

    boolean markEmailAsVerified(@NotNull UUID reqId);

    void disableUser(long userId);
    void renableUser(long userId);

    void changePassword(long userId, @NotNull String password);
    @Nullable Long getUserIdFromPasswordResetReqId(@Nullable UUID pwResetId);
    boolean deletePasswordResetAttempt(@NotNull UUID pwResetId);
    boolean isResetPwRequestPending(@NotNull UUID pwResetId);
    /**
     * @return UUID the id which needs to be opened to reset the password
     */
    UUID initResetPassword(@NotNull String email);
    void resetLoginAttempts(@NotNull String email);
    void increaseLoginAttempts(@NotNull String email);

    int deleteUnverifiedEmailAccounts();
    int deleteExpiredPasswordResets();
}
