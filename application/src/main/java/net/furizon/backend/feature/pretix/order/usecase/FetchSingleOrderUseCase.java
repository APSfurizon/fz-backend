package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.PretixOrder;
import net.furizon.backend.feature.pretix.order.action.deleteOrder.DeleteOrderAction;
import net.furizon.backend.feature.pretix.order.action.upsertOrder.UpsertOrderAction;
import net.furizon.backend.feature.pretix.order.finder.pretix.PretixOrderFinder;
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
    private final UpsertOrderAction insertOrUpdateOrderAction;

    @NotNull
    private final DeleteOrderAction deleteOrderAction;

    @Transactional
    @NotNull
    @Override
    public Optional<Order> executor(@NotNull Input input) {
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
            log.error("[PRETIX] Unable to fetch order: {}@{}", orderCode, eventInfo);
            return Optional.empty();
        }

        var orderOpt = input.pretixInformation.parseOrderFromId(pretixOrder.get(), event);
        if (orderOpt.isEmpty()) {
            deleteOrderAction.invoke(orderCode);
            log.error("[PRETIX] Unable to parse order: {}@{}", orderCode, eventInfo);
            return Optional.empty();
        }

        Order order = orderOpt.get();
        log.debug("[PRETIX] Storing / Updating order: {}@{}", orderCode, eventInfo);
        insertOrUpdateOrderAction.invoke(order, input.pretixInformation);
        return orderOpt;
    }

    public record Input(
        @NotNull Event event,
        @NotNull String code,
        @NotNull PretixInformation pretixInformation
    ) {}
}
