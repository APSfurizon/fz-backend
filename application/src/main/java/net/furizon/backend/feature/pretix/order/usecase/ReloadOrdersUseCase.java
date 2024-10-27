package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.action.delete.DeleteOrderAction;
import net.furizon.backend.feature.pretix.order.action.insertorupdate.InsertOrUpdateOrderAction;
import net.furizon.backend.feature.pretix.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
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
public class ReloadOrdersUseCase implements UseCase<ReloadOrdersUseCase.Input, Boolean>  {
    @NotNull private final PretixOrderFinder pretixOrderFinder;

    @NotNull private final InsertOrUpdateOrderAction insertOrUpdateOrderAction;
    @NotNull private final DeleteOrderAction deleteOrderAction;

    @Transactional
    @NotNull
    @Override
    public Boolean executor(@NotNull Input input) {
        AtomicReference<Boolean> success = new AtomicReference<>(true);
        var eventInfo = input.event.getOrganizerAndEventPair();

        PretixPagingUtil.forEachElement(
            page -> pretixOrderFinder.getPagedOrders(eventInfo.getOrganizer(), eventInfo.getEvent(), page),
            pretixOrder -> {
                boolean shouldDelete = true;

                var orderOpt = input.pretixInformation.parseOrderFromId(pretixOrder.getFirst(), input.event);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    OrderStatus os = order.getOrderStatus();
                    if (os == OrderStatus.PENDING || os == OrderStatus.PAID) {
                        insertOrUpdateOrderAction.invoke(order, input.pretixInformation);
                        shouldDelete = false;
                    }
                }

                if (shouldDelete) {
                    deleteOrderAction.invoke(pretixOrder.getFirst().getCode());
                }
            }
        );

        return success.get();
    }

    public record Input(@NotNull Event event, @NotNull PretixInformation pretixInformation) {}
}
