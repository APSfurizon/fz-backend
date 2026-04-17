package net.furizon.backend.feature.pretix.objects.checkins.finder;

import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.PretixCheckinList;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface PretixCheckinListsFinder {
    @NotNull PretixPaging<PretixCheckinList> getPagedCheckinLists(
            @NotNull String organizer,
            @NotNull String event, int page);
}
