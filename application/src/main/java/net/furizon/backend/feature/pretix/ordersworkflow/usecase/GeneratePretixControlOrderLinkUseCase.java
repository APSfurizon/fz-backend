package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.response.LinkResponse;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneratePretixControlOrderLinkUseCase
        implements UseCase<GeneratePretixControlOrderLinkUseCase.Input, LinkResponse> {
    @NotNull private final PretixConfig pretixConfig;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull LinkResponse executor(@NotNull GeneratePretixControlOrderLinkUseCase.Input input) {
        Order order = orderFinder.findOrderByCodeEvent(
                input.orderCode,
                input.eventId,
                input.pretixInformation
        );
        if (order == null) {
            throw new ApiException(translationService.error("order.not_found"),
                GeneralResponseCodes.ORDER_NOT_FOUND);
        }
        return new LinkResponse(pretixConfig.getShop().getOrderControlUrl(order));
    }

    public record Input(
            long eventId,
            @NotNull String orderCode,
            @NotNull PretixInformation pretixInformation
    ) {}
}
