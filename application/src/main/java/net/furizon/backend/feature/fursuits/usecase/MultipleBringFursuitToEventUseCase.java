package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.FursuitErrorCodes;
import net.furizon.backend.feature.fursuits.action.bringFursuitToEvent.UpdateBringFursuitToEventAction;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.dto.MultipleBringFursuitToEventRequest;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultipleBringFursuitToEventUseCase implements UseCase<MultipleBringFursuitToEventUseCase.Input, Boolean> {
    @NotNull private final UpdateBringFursuitToEventAction updateBringFursuitToEventAction;
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitChecks fursuitChecks;
    @NotNull private final FursuitConfig fursuitConfig;
    @NotNull private final BadgeConfig badgeConfig;

    @NotNull private final TranslationService translationService;

    @Override
    @Transactional
    public @NotNull Boolean executor(@NotNull MultipleBringFursuitToEventUseCase.Input input) {
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();
        Map<Long, Boolean> newSelection = input.req.getFursuitBroughtToEventMap();

        boolean isAdmin = fursuitChecks.isAdmin(input.user.getUserId());
        long ownerUserId = generalChecks.getUserIdAndAssertPermission(input.req.getOwnerUserId(), input.user, null, isAdmin);

        generalChecks.assertTimeframeForEventNotPassedAllowAdmin(badgeConfig.getEditingDeadline(), event, ownerUserId, input.user.getUserId(), null, isAdmin);

        log.info("User {} is updating multiple bringToCurrentEvent of user {}: {}",
                input.user.getUserId(), ownerUserId, newSelection);

        Map<Long, FursuitData> idToFursuit = fursuitFinder.getFursuitsOfUser(ownerUserId, event).stream().collect(Collectors.toMap(f -> f.getFursuit().getId(), f -> f));
        if (!idToFursuit.keySet().containsAll(newSelection.keySet())) {
            log.error("Fursuit(s) not found: {}", new HashSet<Long>(newSelection.keySet()).removeAll(idToFursuit.keySet()));
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    translationService.error("badge.fursuit.not_found"),
                    FursuitErrorCodes.FURSUIT_NOT_FOUND
            );
        }

        Order order = generalChecks.getOrderAndAssertItExists(ownerUserId, event, pretixInformation);

        int maxFursuits = fursuitConfig.getDefaultFursuitsNo() + order.getExtraFursuits();
        long fursuitsBrought = idToFursuit.values().stream().filter(f -> {
            long fursuitId = f.getFursuit().getId();
            if (newSelection.containsKey(fursuitId)) {
                return newSelection.get(fursuitId);
            }
            return f.isBringingToEvent();
        }).count();
        if ((int) fursuitsBrought > Math.min(maxFursuits, (int) fursuitConfig.getMaxExtraFursuits())) {
            log.error("User {} has reached max fursuit badges. Max = {}, newSelection = {}", ownerUserId, maxFursuits, fursuitsBrought);
            throw new ApiException(translationService.error("badge.fursuit.fail.bring_to_event_limit_reached"),
                    FursuitErrorCodes.FURSUIT_BADGES_ENDED);
        }

        boolean checkedOrderStatus = false;
        for (Map.Entry<Long, Boolean> entry : newSelection.entrySet()) {

            long fursuitId = entry.getKey();
            boolean newBroughtToEvent = entry.getValue();
            FursuitData fursuit = idToFursuit.get(fursuitId);
            boolean oldBroughtToEvent = fursuit.isBringingToEvent();

            if (oldBroughtToEvent != newBroughtToEvent) {

                if (newBroughtToEvent && !checkedOrderStatus) {
                    generalChecks.assertOrderIsPaid(order, ownerUserId, event);
                    checkedOrderStatus = true;
                }

                updateBringFursuitToEventAction.invoke(fursuitId, newBroughtToEvent, order);
            }
        }

        return true;
    }

    public record Input(
            MultipleBringFursuitToEventRequest req,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ) {}
}
