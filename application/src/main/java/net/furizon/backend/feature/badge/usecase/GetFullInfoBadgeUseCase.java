package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.fursuits.dto.FursuitListResponse;
import net.furizon.backend.feature.fursuits.usecase.GetAllFursuitsUseCase;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetFullInfoBadgeUseCase implements UseCase<GetFullInfoBadgeUseCase.Input, FullInfoBadgeResponse> {
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull FullInfoBadgeResponse executor(@NotNull GetFullInfoBadgeUseCase.Input input) {
        Event event = input.pretixInformation.getCurrentEvent();

        boolean isAdmin = false;
        if (input.userId != null) {
            isAdmin = permissionFinder.userHasAnyPermission(
                    input.user.getUserId(),
                    Permission.CAN_MANAGE_USER_PUBLIC_INFO,
                    Permission.CAN_PERFORM_CHECKINS
            );
        }
        long userId = generalChecks.getUserIdAndAssertPermission(input.userId, input.user, null, isAdmin);
        log.info("User {} is asking for full info badge of user {}", input.user.getUserId(), userId);

        UserDisplayData userData = generalChecks.assertUserFound(userFinder.getDisplayUser(userId, event));
        OffsetDateTime editingDeadline = badgeConfig.getEditingDeadline();

        boolean allowedModifications = generalChecks.isTimeframeForEventOk(badgeConfig.getEditingDeadline(), event);

        FursuitListResponse fursuits = GetAllFursuitsUseCase.INSTANCE.invoke(userId, event, input.pretixInformation);

        return new FullInfoBadgeResponse(
                userData,
                editingDeadline,
                allowedModifications,
                fursuits
        );
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable Long userId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
