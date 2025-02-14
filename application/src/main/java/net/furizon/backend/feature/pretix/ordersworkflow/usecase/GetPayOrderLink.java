package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
@Slf4j
public class GetPayOrderLink implements UseCase<GetPayOrderLink.Input, LinkResponse> {
    @NotNull private final OrderFinder orderFinder;

    @NotNull private final PretixConfig pretixConfig;

    @Override
    public @NotNull LinkResponse executor(@NotNull GetPayOrderLink.Input input) {
        PretixInformation pretixService = input.pretixService;
        Event event = pretixService.getCurrentEvent();

        int orderNo = orderFinder.countOrdersOfUserOnEvent(input.user.getUserId(), event);
        if (orderNo > 1) {
            throw new RuntimeException("Multiple orders found for the same user in the same event!");
        }

        Order order = orderFinder.findOrderByUserIdEvent(input.user.getUserId(), event, pretixService);
        if (order == null) {
            log.error("Unable to find order for user {} on event {}", input.user.getUserId(), event);
            throw new RuntimeException("Unable to find the order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            log.error("User {} is trying to get the payment link for order {}, but order is in state {}",
                    input.user.getUserId(), order.getCode(), order.getOrderStatus());
        }

        return new LinkResponse(pretixConfig.getShop().getOrderUrl(order) + "pay/change");
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixService
    ) {}
}
