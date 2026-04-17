package net.furizon.backend.feature.pretix.objects.checkins.finder;

import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinSearchOrder;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PretixCheckinSearchFinder {
    @NotNull PretixPaging<PretixPosition> getPagedCheckinSearchResults(
            @NotNull String organizer,

            @Nullable String search,
            @Nullable Long checkinListId,
            @Nullable Boolean hasCheckedIn,
            @Nullable CheckinSearchOrder order,

            @Nullable Integer page);
}
