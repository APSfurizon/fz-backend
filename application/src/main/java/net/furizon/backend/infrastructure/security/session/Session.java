package net.furizon.backend.infrastructure.security.session;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Builder
public class Session {
    @NotNull
    private final UUID id;

    @NotNull
    private final OffsetDateTime createdAt;

    @NotNull
    private final OffsetDateTime expiresAt;
}
