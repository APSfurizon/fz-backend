package net.furizon.backend.feature.badge.finder;

import net.furizon.backend.feature.admin.dto.BadgePrint;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BadgeFinder {
    @Nullable MediaData getMediaDataOfUserBadge(long userId);

    @Nullable MediaData getMediaDataOfFursuitBadge(long fursuitId);

    @NotNull List<BadgePrint> getRegularBadgesToPrint(@NotNull Event event);
}
