package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.bringFursuitToEvent.UpdateBringFursuitToEventAction;
import net.furizon.backend.feature.fursuits.dto.BringFursuitToEventRequest;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
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

    @Override
    public @NotNull Boolean executor(@NotNull BringFursuitToEventUseCase.Input input) {
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        long userId = generalChecks.getUserIdAndAssertPermission(input.req.getUserId(), input.user);
        log.info("User {} is setting bringToCurrentEvent = {} on fursuit {}",
                input.user.getUserId(), input.req.isBringFursuitToCurrentEvent(), input.fursuitId);
        fursuitChecks.assertUserHasPermissionOnFursuit(userId, input.fursuitId);

        boolean res;
        Order order = generalChecks.getOrderAndAssertItExists(userId, event, pretixInformation);
        if (input.req.isBringFursuitToCurrentEvent()) {

            generalChecks.assertOrderIsPaid(order, userId, event);
            fursuitChecks.assertFursuitNotAlreadyBroughtToCurrentEvent(input.fursuitId, order);
            fursuitChecks.assertUserHasNotReachedMaxFursuitBadges(userId, order);
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
