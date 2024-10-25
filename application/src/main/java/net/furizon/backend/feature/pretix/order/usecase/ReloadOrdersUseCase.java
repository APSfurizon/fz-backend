package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.action.delete.DeleteOrderAction;
import net.furizon.backend.feature.pretix.order.action.insertorupdate.InsertOrUpdateOrderAction;
import net.furizon.backend.feature.pretix.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This use case should get all orders from Pretix
 * Insert it to Database if not exist there (yet)
 * Returns true on success
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReloadOrdersUseCase implements UseCase<Event, Boolean>  {
    @NotNull private final PretixOrderFinder pretixOrderFinder;

    @NotNull private final PretixInformation pretixInformation;

    @NotNull private final InsertOrUpdateOrderAction insertOrUpdateOrderAction;
    @NotNull private final DeleteOrderAction deleteOrderAction;

    @Transactional
    @NotNull
    @Override
    public Boolean executor(@NotNull Event event) {
        AtomicReference<Boolean> success = new AtomicReference<>(true);
        var eventInfo = event.getOrganizerAndEventPair();

        PretixPagingUtil.forEachElement(
            page -> pretixOrderFinder.getPagedOrders(eventInfo.getOrganizer(), eventInfo.getEvent(), page),
            pretixOrder -> {
                var order = pretixInformation.parseOrderFromId(pretixOrder.getFirst(), event);
                if (order.isPresent()) {
                    insertOrUpdateOrderAction.invoke(order.get());
                } else {
                    deleteOrderAction.invoke(pretixOrder.getFirst().getCode());
                }
            }
        );

        return success.get();
    }
}
