package net.furizon.backend.infrastructure.security;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Data
@ConfigurationProperties("security")
public class SecurityConfig {
    private final long unverifiedEmailExpireHours;
    private final long unverifiedPasswordResetExpireHours;

    @NotNull
    private final String passwordSalt;

    @NotNull
    private final String tokenSecretKey;

    @NotNull
    private final List<String> allowedOrigins;

    @NotNull
    private final Session session;

    @Data
    public static class Session {
        @NotNull
        private final Duration expiration;

        private final int maxAllowedSessionsSize;

        private final int corePoolUpdateSize;
    }
}
