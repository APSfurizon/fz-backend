package net.furizon.backend.feature.pretix.objects.event.finder.pretix;

import net.furizon.backend.feature.pretix.objects.event.PretixEvent;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface PretixEventFinder {
    @NotNull
    PretixPaging<PretixEvent> getPagedEvents(@NotNull String organizer, int page);
}
