package net.furizon.backend.feature.badge.dto;

import lombok.Data;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class FullInfoBadgeResponse {
    @NotNull private final UserDisplayData mainBadge;
    @NotNull private final OffsetDateTime badgeEditingDeadline;

    @NotNull private final List<FursuitDisplayData> fursuits;
    private final short bringingToEvent;
    private final short maxFursuits;

    private final boolean canBringFursuitsToEvent;
}
