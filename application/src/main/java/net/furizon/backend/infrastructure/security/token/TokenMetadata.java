package net.furizon.backend.infrastructure.security.token;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
public class TokenMetadata {
    private final UUID sessionId;
}
