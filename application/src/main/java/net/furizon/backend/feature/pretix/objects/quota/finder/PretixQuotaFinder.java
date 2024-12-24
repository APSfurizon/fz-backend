package net.furizon.backend.feature.pretix.objects.quota.finder;

import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface PretixQuotaFinder {
    @NotNull
    PretixPaging<PretixQuota> getPagedQuotas(
            @NotNull String organizer,
            @NotNull String event,
            int page
    );
}
