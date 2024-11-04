package net.furizon.backend.infrastructure.security;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("security")
public class SecurityConfig {
    @NotNull
    private final String passwordSalt;
}
