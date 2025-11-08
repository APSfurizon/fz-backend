package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.bringFursuitToEvent.UpdateBringFursuitToEventAction;
import net.furizon.backend.feature.fursuits.dto.BringFursuitToEventRequest;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BringFursuitToEventUseCase implements UseCase<BringFursuitToEventUseCase.Input, Boolean> {
    @NotNull private final UpdateBringFursuitToEventAction updateBringFursuitToEventAction;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitChecks fursuitChecks;
    @NotNull private final BadgeConfig badgeConfig;

    @Override
    public @NotNull Boolean executor(@NotNull BringFursuitToEventUseCase.Input input) {
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        long userId = input.user.getUserId();

        log.info("User {} is setting bringToCurrentEvent = {} on fursuit {}",
                userId, input.req.getBringFursuitToCurrentEvent(), input.fursuitId);
        // We cannot allow the editing of bringFursuitToEvent after the event has ended
        fursuitChecks.assertPermissionAndTimeframe(userId, input.fursuitId, null, badgeConfig.getEditingDeadline());

        FursuitData fursuit = fursuitChecks.getFursuitAndAssertItExists(input.fursuitId, event, userId, true);
        long ownerId = fursuit.getOwnerId();

        boolean res;
        Order order = generalChecks.getOrderAndAssertItExists(ownerId, event, pretixInformation);
        if (input.req.getBringFursuitToCurrentEvent()) {

            generalChecks.assertOrderIsPaid(order, ownerId, event);
            fursuitChecks.assertFursuitNotAlreadyBroughtToCurrentEvent(input.fursuitId, order);
            fursuitChecks.assertUserHasNotReachedMaxFursuitBadges(ownerId, order);
            res = updateBringFursuitToEventAction.invoke(
                    input.fursuitId,
                    true,
                    order
            );
        } else {

            fursuitChecks.assertFursuitIsBroughtToCurrentEvent(input.fursuitId, order);
            res = updateBringFursuitToEventAction.invoke(
                input.fursuitId,
                false,
                order
            );
        }

        return res;
    }

    public record Input(
            BringFursuitToEventRequest req,
            long fursuitId,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ) {}
}
