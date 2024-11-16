package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePretixShopLink implements UseCase<GeneratePretixShopLink.Input, LinkResponse> {
    @NotNull private final OrderFinder orderFinder;

    @Override
    public @NotNull LinkResponse executor(@NotNull GeneratePretixShopLink.Input input) {
        int ordersNo = orderFinder.countOrdersOfUserOnEvent(input.user.getUserId(), input.event);
        if (ordersNo > 0) {
            throw new ApiException(
                    "You already made an order!",
                    OrderWorkflowErrorCode.ORDER_ALREADY_DONE.name()
            );
        }


        return null;
    }

    public record Input(FurizonUser user, Event event) {}
}