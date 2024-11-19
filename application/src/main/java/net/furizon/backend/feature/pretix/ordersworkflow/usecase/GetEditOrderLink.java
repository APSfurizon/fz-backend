package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
@Slf4j
public class GetEditOrderLink implements UseCase<GetEditOrderLink.Input, LinkResponse> {
    @NotNull private final OrderFinder orderFinder;

    @NotNull
    @Value("${pretix.shop.url}")
    private String shopUrl = "";

    @Override
    public @NotNull LinkResponse executor(@NotNull GetEditOrderLink.Input input) {
        PretixInformation pretixService = input.pretixService;
        var e = pretixService.getCurrentEvent();
        if (!e.isPresent()) {
            throw new RuntimeException("Unable to load current event");
        }
        Event event = e.get();

        int orderNo = orderFinder.countOrdersOfUserOnEvent(input.user.getUserId(), event);
        if (orderNo > 1) {
            throw new RuntimeException("Multiple orders found for the same user in the same event!");
        }

        Order order = orderFinder.findOrderByUserIdEvent(input.user.getUserId(), event, pretixService);
        if (order == null) {
            log.error("Unable to find order for user {} on event {}", input.user.getUserId(), event);
            throw new RuntimeException("Unable to find the order");
        }

        return new LinkResponse(shopUrl + "order/" + order.getCode() + "/" + order.getPretixOrderSecret());
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixService) {}
}
