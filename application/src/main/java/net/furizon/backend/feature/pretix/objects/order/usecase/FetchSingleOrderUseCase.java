package net.furizon.backend.feature.pretix.objects.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.deleteOrder.DeleteOrderAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FetchSingleOrderUseCase implements UseCase<FetchSingleOrderUseCase.Input, Optional<Order>> {
    @NotNull
    private final PretixOrderFinder pretixOrderFinder;

    @NotNull
    private final DeleteOrderAction deleteOrderAction;

    @NotNull
    private final UpdateOrderInDb updateOrderInDb;

    @Transactional
    @NotNull
    @Override
    public Optional<Order> executor(@NotNull Input input) {
        try {
            OrderController.suspendWebhook();
            Event event = input.event;
            String orderCode = input.code;
            var eventInfo = event.getOrganizerAndEventPair();

            Optional<PretixOrder> pretixOrder = pretixOrderFinder.fetchOrderByCode(
                    eventInfo.getOrganizer(),
                    eventInfo.getEvent(),
                    orderCode
            );

            if (pretixOrder.isEmpty()) {
                deleteOrderAction.invoke(orderCode);
                log.error("[PRETIX] Unable to fetch order: {}@{}", orderCode, event.getSlug());
                return Optional.empty();
            }

            return updateOrderInDb.execute(pretixOrder.get(), event, input.pretixInformation);
        } finally {
            OrderController.resumeWebhook();
        }
    }

    public record Input(
        @NotNull Event event,
        @NotNull String code,
        @NotNull PretixInformation pretixInformation
    ) {}
}
