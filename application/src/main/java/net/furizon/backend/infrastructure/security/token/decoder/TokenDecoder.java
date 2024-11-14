package net.furizon.backend.infrastructure.security.token.decoder;

import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;

public interface TokenDecoder {
    @NotNull TokenMetadata decode(@NotNull String token);
}
