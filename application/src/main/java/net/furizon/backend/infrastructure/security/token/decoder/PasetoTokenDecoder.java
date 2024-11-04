package net.furizon.backend.infrastructure.security.token.decoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;
import org.paseto4j.commons.PasetoException;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PasetoTokenDecoder implements TokenDecoder {
    private final ObjectMapper objectMapper;

    private final SecretKey secretKey;

    public PasetoTokenDecoder(SecurityConfig securityConfig, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.secretKey = new SecretKey(
            securityConfig.getTokenSecretKey().getBytes(StandardCharsets.UTF_8),
            Version.V4
        );
    }

    @Override
    public @NotNull TokenMetadata decode(@NotNull String token) {
        try {
            final var json = Paseto.decrypt(
                secretKey,
                token,
                "Furizon Team"
            );

            return objectMapper.readValue(json, TokenMetadata.class);
        } catch (JsonProcessingException ex) {
            log.warn("Could read json", ex);
            throw new AuthenticationServiceException("Bad message", ex);
        } catch (PasetoException e) {
            log.warn("Could not decode token", e);
            throw new SessionAuthenticationException("Invalid Session Token");
        }
    }
}
