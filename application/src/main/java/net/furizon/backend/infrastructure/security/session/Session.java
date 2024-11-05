package net.furizon.backend.infrastructure.security.session;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Builder
public class Session {
    private final UUID id;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime expiresAt;
}
