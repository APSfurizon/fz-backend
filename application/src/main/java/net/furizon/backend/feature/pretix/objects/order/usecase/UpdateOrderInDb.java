package net.furizon.backend.feature.pretix.objects.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.deleteOrder.DeleteOrderAction;
import net.furizon.backend.feature.pretix.objects.order.action.upsertOrder.UpsertOrderAction;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderInDb {

    @NotNull
    private final UpsertOrderAction upsertOrderAction;

    @NotNull
    private final DeleteOrderAction deleteOrderAction;

    public @NotNull Optional<Order> execute(@NotNull PretixOrder order,
                                             @NotNull Event event,
                                             @NotNull PretixInformation pretixInformation) {
        Order ret = null;
        boolean shouldDelete = true;

        var orderOpt = pretixInformation.parseOrder(order, event);
        if (orderOpt.isPresent()) {
            ret = orderOpt.get();
            OrderStatus os = ret.getOrderStatus();
            if (os == OrderStatus.PENDING || os == OrderStatus.PAID) {
                log.debug("[PRETIX] Storing / Updating order: {}@{}", order.getCode(), event.getSlug());
                upsertOrderAction.invoke(ret, pretixInformation);
                shouldDelete = false;
            }
        } else {
            log.error("[PRETIX] Unable to parse order: {}@{}", order.getCode(), event.getSlug());
        }

        if (shouldDelete) {
            deleteOrderAction.invoke(order.getCode());
        }

        return Optional.ofNullable(ret);
    }
}
