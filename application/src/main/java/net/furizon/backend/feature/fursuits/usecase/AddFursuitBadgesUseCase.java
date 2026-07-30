package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitErrorCodes;
import net.furizon.backend.feature.fursuits.dto.AddFursuitBadgesRequest;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.action.addPositionWithPayment.AddPositionWithPaymentAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddFursuitBadgesUseCase implements UseCase<AddFursuitBadgesUseCase.Input, Boolean> {
    @NotNull private final AddPositionWithPaymentAction action;

    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final UpdateOrderInDb updateOrderInDb;

    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitConfig fursuitConfig;

    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull AddFursuitBadgesUseCase.Input input) {
        try {
            OrderController.suspendWebhook();
            AddFursuitBadgesRequest req = input.req;
            Event event = input.pretixInformation.getCurrentEvent();

            log.info("Admin {} is adding {} badges to {}. Mark as paid = {}",
                    input.user.getUserId(), req.getQuantity(), req.getTargetUserId(), req.getAlreadyPaid());

            Order order = generalChecks.getOrderAndAssertItExists(
                    req.getTargetUserId(),
                    event, input.pretixInformation
            );
            int newExtraFursuits = fursuitConfig.getDefaultFursuitsNo() + order.getExtraFursuits() + req.getQuantity();
            if (newExtraFursuits > fursuitConfig.getMaxExtraFursuits()) {
                log.error("With {} new fursuit badges, the order would surpass the max extra fursuits no ({} > {})",
                        req.getQuantity(), newExtraFursuits, fursuitConfig.getMaxExtraFursuits());
                throw new ApiException(
                        translationService.error(
                                "badge.fursuit.fail.too_many_extra_fursuits",
                                fursuitConfig.getMaxExtraFursuits()
                        ),
                        FursuitErrorCodes.TOO_MANY_EXTRA_FURSUIT_BADGES
                );
            }

            Set<Long> itemIds = input.pretixInformation.getIdsForItemType(CacheItemTypes.EXTRA_FURSUITS);

            boolean result = action.invoke(
                    order,
                    input.pretixInformation,
                    itemIds.iterator().next(),
                    req.getAlreadyPaid(),
                    req.getQuantity(),
                    order.getMainPositionId(),
                    null,
                    null,
                    true,
                    true
            );

            //Refresh order in db
            if (result) {
                var pair = event.getOrganizerAndEventPair();
                var pretixOrder = pretixOrderFinder.fetchOrderByCode(
                        pair.getOrganizer(), pair.getEvent(),
                        order.getCode()
                );
                if (pretixOrder.isPresent()) {
                    var updatedOrder = updateOrderInDb.execute(pretixOrder.get(), event, input.pretixInformation);
                    if (updatedOrder.isEmpty()) {
                        log.error("Unable to update order {} in the database", order.getCode());
                        result = false;
                    }
                } else {
                    log.error("Unable to fetch order {} from pretix", order.getCode());
                    result = false;
                }
            }

            return result;
        } finally {
            OrderController.resumeWebhook();
        }
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation,
            @NotNull AddFursuitBadgesRequest req
    ) {}
}
