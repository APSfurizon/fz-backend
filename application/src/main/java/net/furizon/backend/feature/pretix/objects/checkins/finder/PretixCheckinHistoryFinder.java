package net.furizon.backend.feature.pretix.objects.checkins.finder;

import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.CheckinType;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.PagedPretixCheckinHistory;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinHistoryOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public interface PretixCheckinHistoryFinder {
    @NotNull PagedPretixCheckinHistory getPagedCheckinLists(
            @NotNull String organizer,
            @NotNull String event,

            @Nullable OffsetDateTime createdSince,
            @Nullable OffsetDateTime createdBefore,
            @Nullable OffsetDateTime datetimeSince,
            @Nullable OffsetDateTime datetimeBefore,
            @Nullable Boolean successful,
            @Nullable Long checkinListId,
            @Nullable CheckinType type,
            @Nullable Boolean autoCheckedIn,
            @Nullable CheckinHistoryOrder order,

            @Nullable Integer page);
}
