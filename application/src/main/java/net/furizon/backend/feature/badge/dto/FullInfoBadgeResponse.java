package net.furizon.backend.feature.badge.dto;

import lombok.Data;
import net.furizon.backend.feature.fursuits.dto.FursuitListResponse;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class FullInfoBadgeResponse {
    @NotNull private final UserDisplayData mainBadge;
    @Nullable private final OffsetDateTime badgeEditingDeadline;
    private final boolean allowedModifications;

    @NotNull
    private final FursuitListResponse fursuits;
}
