package net.furizon.backend.feature.pretix.order.usecase;

import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.PretixOrder;
import net.furizon.backend.feature.pretix.order.action.delete.DeleteOrderAction;
import net.furizon.backend.feature.pretix.order.action.insertorupdate.InsertOrUpdateOrderAction;
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
public class FetchSingleOrderUseCase implements UseCase<Pair<Event, String>, Optional<Order>>  {
    @NotNull private final PretixOrderFinder pretixOrderFinder;

    @NotNull private final PretixInformation pretixInformation;

    @NotNull private final InsertOrUpdateOrderAction insertOrUpdateOrderAction;
    @NotNull private final DeleteOrderAction deleteOrderAction;

    @Transactional
    @NotNull
    @Override
    public Optional<Order> executor(@NotNull Pair<Event, String> info) {
        Event event = info.getFirst();
        String orderCode = info.getSecond();
        var eventInfo = event.getOrganizerAndEventPair();

        Optional<PretixOrder> pretixOrder = pretixOrderFinder.fetchOrderByCode(
                eventInfo.getOrganizer(),
                eventInfo.getEvent(),
                orderCode
        );
        if (pretixOrder.isPresent()) {
            var orderOpt = pretixInformation.parseOrderFromId(pretixOrder.get(), event);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                insertOrUpdateOrderAction.invoke(order);
                return Optional.of(order);
            } else {
                deleteOrderAction.invoke(orderCode);
            }
        } else {
            deleteOrderAction.invoke(orderCode);
        }
        return Optional.empty();
    }
}
