package net.furizon.backend.infrastructure.security.session;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Builder
public class Session {
    @NotNull
    private final UUID id;

    @NotNull
    private final String createdByIpAddress;

    @NotNull
    private final String lastUsedByIpAddress;

    @Nullable
    private final String userAgent;

    @NotNull
    private final OffsetDateTime createdAt;

    @NotNull
    private final OffsetDateTime modifiedAt;

    @NotNull
    private final OffsetDateTime expiresAt;
}
