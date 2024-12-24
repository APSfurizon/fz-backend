package net.furizon.backend.feature.pretix.objects.quota.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.feature.pretix.objects.quota.finder.PretixQuotaFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReloadQuotaUseCase implements UseCase<Event, List<PretixQuota>> {
    @NotNull private final PretixQuotaFinder quotaFinder;

    @NotNull
    @Override
    public List<PretixQuota> executor(@NotNull Event input) {
        final var pair = input.getOrganizerAndEventPair();

        return PretixPagingUtil.fetchAll(
                paging -> quotaFinder.getPagedQuotas(
                        pair.getOrganizer(),
                        pair.getEvent(),
                        paging
                )
        );
    }
}
