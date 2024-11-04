package net.furizon.backend.infrastructure.security;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties("security")
public class SecurityConfig {
    @NotNull
    private final String passwordSalt;

    @NotNull
    private final String tokenSecretKey;

    @NotNull
    private final Session session;

    @Data
    public static class Session {
        @NotNull
        private final Duration expiration;
    }
}
