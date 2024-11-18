package net.furizon.backend.feature.pretix.objects.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This use case should get all orders from Pretix
 * Insert it to Database if not exist there (yet)
 * Returns true on success
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReloadOrdersUseCase implements UseCase<ReloadOrdersUseCase.Input, Boolean> {
    @NotNull
    private final PretixOrderFinder pretixOrderFinder;

    private final UpdateOrderInDb updateOrderInDb;

    @Transactional
    @NotNull
    @Override
    public Boolean executor(@NotNull Input input) {
        var eventInfo = input.event.getOrganizerAndEventPair();

        PretixPagingUtil.forEachElement(
            page -> pretixOrderFinder.getPagedOrders(eventInfo.getOrganizer(), eventInfo.getEvent(), page),
            pretixOrder -> updateOrderInDb.execute(
                pretixOrder.getLeft(),
                input.event,
                input.pretixInformation
            )
        );

        return true;
    }

    public record Input(
        @NotNull Event event,
        @NotNull PretixInformation pretixInformation
    ) {}
}
