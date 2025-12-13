package net.furizon.backend.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.session.exception.SessionExpiredException;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.security.token.decoder.TokenDecoder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.concurrent.Executor;

import static net.furizon.backend.infrastructure.security.Const.SESSION_THREAD_POOL_TASK_EXECUTOR;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSessionFilter extends OncePerRequestFilter {
    private final TokenDecoder tokenDecoder;

    @Qualifier(SESSION_THREAD_POOL_TASK_EXECUTOR)
    private final Executor sessionExecutor;

    private final SessionAuthenticationManager sessionAuthenticationManager;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.matches("(?i)^Bearer .*")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final var tokenMetadata = tokenDecoder.decode(
                authHeader.replaceFirst("(?i)^Bearer ", "")
            );
            long userId = tokenMetadata.getUserId();
            final var pair = sessionAuthenticationManager.findSessionWithAuthenticationById(
                tokenMetadata.getSessionId()
            );
            if (pair == null) {
                throw new SessionAuthenticationException("Session not found");
            }
            final var session = pair.getLeft();
            final var sessionId = session.getId();
            final var expiresAt = session.getExpiresAt();
            // Check if session has expired
            if (expiresAt.isBefore(OffsetDateTime.now())) {
                sessionExecutor.execute(() -> {
                    log.debug("Session '{}' expired, deleting", sessionId);
                    sessionAuthenticationManager.deleteSession(sessionId);
                });
                throw new SessionExpiredException("The session has expired.");
            }

            SecurityContextHolder
                .getContext()
                .setAuthentication(
                    new PreAuthenticatedAuthenticationToken(
                        FurizonUser.builder()
                            .userId(userId)
                            .sessionId(sessionId)
                            .authentication(pair.getMiddle())
                            .language(pair.getRight())
                            .build(),
                        null,
                        Collections.emptyList() // TODO -> Implement authorities
                    )
                );

            final var clientIp = request.getRemoteAddr();
            sessionExecutor.execute(() -> sessionAuthenticationManager.updateSession(session, clientIp, userId));
        } catch (AuthenticationException ex) {
            log.error("Authentication failed", ex);
            SecurityContextHolder.clearContext();
            authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
