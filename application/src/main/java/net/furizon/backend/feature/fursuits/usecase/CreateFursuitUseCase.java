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
import net.furizon.backend.infrastructure.security.permissions.Permission;
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
    @NotNull private final BadgeConfig badgeConfig;

    @Override
    public @NotNull FursuitData executor(@NotNull Input input) {
        Order order = null;
        //Ideally we can limit the interaction (CRUD) just for fursuits which are NOT brought to current event
        // and also disallow people from changing the bringToCurrentEvent flag. However this is not so trivial,
        // to implement, so we just globally disable the editing of fursuits from the deadline to the end of the event
        Event e = input.pretixInformation.getCurrentEvent();
        long targetUserId = generalChecks.getUserIdAssertPermissionCheckTimeframe(
                input.userId,
                input.user,
                Permission.CAN_MANAGE_USER_PUBLIC_INFO,
                badgeConfig.getEditingDeadline(),
                //we cannot create fursuits with bringToCurrentEvenet after the event has ended
                input.bringToCurrentEvenet ? null : e
        );
        log.info("User {} is creating fursuit {} for user {}", input.user.getUserId(), targetUserId, input.name);

        fursuitChecks.assertUserHasNotReachedMaxBackendFursuitNo(targetUserId);

        if (input.bringToCurrentEvenet) {
            order = generalChecks.getOrderAndAssertItExists(
                    targetUserId,
                    e,
                    input.pretixInformation
            );
            generalChecks.assertOrderIsPaid(order, targetUserId, e);

            fursuitChecks.assertUserHasNotReachedMaxFursuitBadges(targetUserId, order);
        }

        long fursuitId = createFursuitAction.invoke(
                targetUserId,
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
                .ownerId(targetUserId)
                .build();
        return FursuitData.builder()
                .bringingToEvent(input.bringToCurrentEvenet)
                .showInFursuitCount(input.showInFursuitCount)
                .showOwner(input.showOwner)
                .ownerId(targetUserId)
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
