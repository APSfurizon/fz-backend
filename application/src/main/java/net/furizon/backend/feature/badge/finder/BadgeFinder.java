package net.furizon.backend.feature.badge.finder;

import net.furizon.backend.feature.badge.dto.UserBadgePrint;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BadgeFinder {
    @Nullable MediaData getMediaDataOfUserBadge(long userId);

    @Nullable MediaData getMediaDataOfFursuitBadge(long fursuitId);

    @NotNull List<UserBadgePrint> getUserBadgesToPrint(
            @NotNull Event event,
            @Nullable String orderCodes,
            @Nullable String orderSerials,
            @Nullable String userIds
    );
}
