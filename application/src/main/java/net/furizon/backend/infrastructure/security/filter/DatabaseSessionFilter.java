package net.furizon.backend.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.token.decoder.TokenDecoder;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSessionFilter extends OncePerRequestFilter {
    private final TokenDecoder tokenDecoder;

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
            // TODO -> get sessionId and find session in database to validate it

            SecurityContextHolder
                .getContext()
                .setAuthentication(
                    new PreAuthenticatedAuthenticationToken(
                        FurizonUser.builder()
                            .userId(tokenMetadata.getUserId())
                            .sessionId(tokenMetadata.getSessionId())
                            .build(),
                        null,
                        List.of()
                    )
                );
        } catch (AuthenticationException ex) {
            // TODO -> send fail error if exception
            log.error("Authentication failed", ex);
            SecurityContextHolder.clearContext();
            //return;
        }

        filterChain.doFilter(request, response);
    }
}
