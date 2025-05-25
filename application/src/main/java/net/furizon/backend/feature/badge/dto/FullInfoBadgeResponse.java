package net.furizon.backend.feature.badge.dto;

import lombok.Data;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class FullInfoBadgeResponse {
    @NotNull private final UserDisplayData mainBadge;
    @Nullable private final OffsetDateTime badgeEditingDeadline;
    private final boolean allowedModifications;

    @NotNull private final List<FursuitData> fursuits;
    private final short bringingToEvent;
    private final short maxFursuits;

    private final boolean canBringFursuitsToEvent;
}
