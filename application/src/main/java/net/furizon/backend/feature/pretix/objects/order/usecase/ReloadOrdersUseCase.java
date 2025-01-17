package net.furizon.backend.feature.pretix.objects.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.action.deleteOrder.DeleteOrderAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This use case should get all orders from Pretix
 * Insert it to Database if not exist there (yet)
 * Returns true on success
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReloadOrdersUseCase implements UseCase<ReloadOrdersUseCase.Input, Boolean> {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    private final DeleteOrderAction deleteOrderAction;

    @Transactional
    @NotNull
    @Override
    public Boolean executor(@NotNull Input input) {
        try {
            OrderController.suspendWebhook();
            var eventInfo = input.event.getOrganizerAndEventPair();

            Set<String> pretixOrderCodes = new HashSet<>();
            PretixPagingUtil.forEachElement(
                    page -> pretixOrderFinder.getPagedOrders(eventInfo.getOrganizer(), eventInfo.getEvent(), page),
                    pretixOrder -> {
                        Optional<Order> order = updateOrderInDb.execute(
                                pretixOrder.getLeft(),
                                input.event,
                                input.pretixInformation
                        );
                        if (order.isPresent()) {
                            pretixOrderCodes.add(order.get().getCode());
                        }
                    }
            );

            //Find which orders are still stored in the DB but don't exist anymore on pretix
            Set<String> dbOrderCodes = orderFinder.findOrderCodesForEvent(input.event);
            dbOrderCodes.removeAll(pretixOrderCodes);

            if (!dbOrderCodes.isEmpty()) {
                log.info(
                        "[PRETIX] Removing the following orders from DB since "
                        + "they can't be found anymore on pretix: {}",
                        dbOrderCodes
                );
                deleteOrderAction.invoke(dbOrderCodes);
            }

            return true;
        } finally {
            OrderController.resumeWebhook();
        }
    }

    public record Input(
        @NotNull Event event,
        @NotNull PretixInformation pretixInformation
    ) {}
}
