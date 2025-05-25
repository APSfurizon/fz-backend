package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.createFursuit.CreateFursuitAction;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
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
public class CreateFursuitUseCase implements UseCase<CreateFursuitUseCase.Input, FursuitData> {
    @NotNull private final CreateFursuitAction createFursuitAction;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitChecks fursuitChecks;
    @NotNull private final BadgeConfig badgeConfig;

    @Override
    public @NotNull FursuitData executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        log.info("User {} is creating fursuit {}", userId, input.name);

        fursuitChecks.assertUserHasNotReachedMaxBackendFursuitNo(userId);

        Order order = null;
        //Ideally we can limit the interaction (CRUD) just for fursuits which are NOT brought to current event
        // and also disallow people from changing the bringToCurrentEvent flag. However this is not so trivial,
        // to implement, so we just globally disable the editing of fursuits from the deadline to the end of the event
        Event e = input.pretixInformation.getCurrentEvent();
        generalChecks.assertTimeframeForEventNotPassed(badgeConfig.getEditingDeadline(), e);
        if (input.bringToCurrentEvenet) {
            order = generalChecks.getOrderAndAssertItExists(
                    userId,
                    e,
                    input.pretixInformation
            );
            generalChecks.assertOrderIsPaid(order, userId, e);

            fursuitChecks.assertUserHasNotReachedMaxFursuitBadges(userId, order);
        }

        long fursuitId = createFursuitAction.invoke(
                userId,
                input.name,
                input.species,
                input.showInFursuitCount,
                input.showOwner,
                order
        );

        FursuitDisplayData fursuit = FursuitDisplayData.builder()
                .id(fursuitId)
                .name(input.name)
                .species(input.species)
                .ownerId(userId)
                .build();
        return FursuitData.builder()
                .bringingToEvent(input.bringToCurrentEvenet)
                .showInFursuitCount(input.showInFursuitCount)
                .showOwner(input.showOwner)
                .ownerId(userId)
                .fursuit(fursuit)
            .build();
    }

    public record Input(
            @NotNull String name,
            @NotNull String species,
            boolean bringToCurrentEvenet,
            boolean showInFursuitCount,
            boolean showOwner,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ){}
}
