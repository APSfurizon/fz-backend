package net.furizon.backend.infrastructure.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties("security")
public class SecurityConfig {
    @NotNull
    private final String passwordSalt;
}
