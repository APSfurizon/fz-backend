package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.ManualLinkOrderRequest;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManualOrderLinkUseCase implements UseCase<ManualOrderLinkUseCase.Input, Boolean> {
    @NotNull private final OrderFinder orderFinder;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        ManualLinkOrderRequest request = input.request;
        log.info("User {} is trying to link order {} to user {}",
                input.user.getUserId(), request.getOrderCode(), request.getUserId());

        try {
            OrderController.suspendWebhook();

        } finally {
            OrderController.resumeWebhook();
        }

        return null;
    }

    public record Input(
            @NotNull ManualLinkOrderRequest request,
            @NotNull FurizonUser user,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {}
}
