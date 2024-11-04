package net.furizon.backend.infrastructure.security.token.encoder;

import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PasetoTokenEncoder implements TokenEncoder {
    private final JsonSerializer jsonSerializer;

    private final SecretKey secretKey;

    public PasetoTokenEncoder(SecurityConfig securityConfig, JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        this.secretKey = new SecretKey(
            securityConfig.getTokenSecretKey().getBytes(StandardCharsets.UTF_8),
            Version.V4
        );
    }


    @Override
    public @NotNull String encode(@NotNull TokenMetadata metadata) {
        return Paseto.encrypt(
            secretKey,
            jsonSerializer.serializeAsString(metadata),
            "Furizon Team"
        );
    }
}
