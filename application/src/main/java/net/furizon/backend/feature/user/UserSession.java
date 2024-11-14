package net.furizon.backend.feature.user;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserSession {
    @NotNull
    private final UUID sessionId;

    @Nullable
    private final String userAgent;

    @NotNull
    private final OffsetDateTime createdAt;

    @NotNull
    private final OffsetDateTime lastUsageAt;
}
