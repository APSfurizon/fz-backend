package net.furizon.backend.feature.pretix.objects.quota.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixQuotaFinder {
    @NotNull
    PretixPaging<PretixQuota> getPagedQuotas(
            @NotNull String organizer,
            @NotNull String event,
            int page
    );

    @NotNull
    Optional<PretixQuotaAvailability> getAvailability(
            @NotNull Event event,
            long quotaId
    );
}
