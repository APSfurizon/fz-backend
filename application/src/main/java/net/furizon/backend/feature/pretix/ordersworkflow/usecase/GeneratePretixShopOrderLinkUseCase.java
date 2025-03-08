package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneratePretixShopOrderLinkUseCase
        implements UseCase<GeneratePretixShopOrderLinkUseCase.Input, LinkResponse> {
    @NotNull private final PretixConfig pretixConfig;
    @NotNull private final OrderFinder orderFinder;

    @Override
    public @NotNull LinkResponse executor(@NotNull GeneratePretixShopOrderLinkUseCase.Input input) {
        Order order = orderFinder.findOrderById(input.orderId, input.pretixInformation);
        if (order == null) {
            throw new ApiException("Order not found", GeneralResponseCodes.ORDER_NOT_FOUND);
        }
        return new LinkResponse(pretixConfig.getShop().getOrderControlUrl(order));
    }

    public record Input(
            long orderId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
