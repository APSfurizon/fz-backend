package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.createFursuit.CreateFursuitAction;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateFursuitUseCase implements UseCase<CreateFursuitUseCase.Input, FursuitData> {
    @NotNull private final CreateFursuitAction createFursuitAction;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitChecks fursuitChecks;

    @Override
    public @NotNull FursuitData executor(@NotNull Input input) {
        long userId = generalChecks.getUserIdAndAssertPermission(input.userId, input.user);
        log.info("User {} is creating fursuit {} for user {}", input.user.getUserId(), userId, input.name);

        fursuitChecks.assertUserHasNotReachedMaxBackendFursuitNo(userId);

        Order order = null;
        if (input.bringToCurrentEvenet) {
            Event e = input.pretixInformation.getCurrentEvent();
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
            @Nullable Long userId,
            boolean showOwner,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ){}
}
