package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Data
@Component
@RequiredArgsConstructor
@Slf4j
public class GetEditOrderLink implements UseCase<GetEditOrderLink.Input, LinkResponse> {
    @NotNull private final OrderFinder orderFinder;

    @NotNull private final PretixConfig pretixConfig;

    @Override
    public @NotNull LinkResponse executor(@NotNull GetEditOrderLink.Input input) {
        PretixInformation pretixService = input.pretixService;
        Event event = pretixService.getCurrentEvent();

        OffsetDateTime editEnd = pretixConfig.getEvent().getEditBookingEndTime();
        if (editEnd != null && (editEnd.isBefore(OffsetDateTime.now()))) {
            log.error("User requested the link to edit his order after the editing period is over!");
            throw new ApiException("Order editing is closed!", OrderWorkflowErrorCode.ORDER_EDITS_CLOSED);
        }

        int orderNo = orderFinder.countOrdersOfUserOnEvent(input.user.getUserId(), event);
        if (orderNo > 1) {
            throw new RuntimeException("Multiple orders found for the same user in the same event!");
        }

        Order order = orderFinder.findOrderByUserIdEvent(input.user.getUserId(), event, pretixService);
        if (order == null) {
            log.error("Unable to find order for user {} on event {}", input.user.getUserId(), event);
            throw new RuntimeException("Unable to find the order");
        }

        return new LinkResponse(pretixConfig.getShop().getOrderUrl(order) + "change");
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixService
    ) {}
}
