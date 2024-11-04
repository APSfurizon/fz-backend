package net.furizon.backend.infrastructure.security.token.encoder;

import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;

public interface TokenEncoder {
    @NotNull String encode(@NotNull TokenMetadata metadata);
}
