package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.action.updateFursonaName.UpdateFursonaNameAction;
import net.furizon.backend.feature.badge.dto.UpdateFursonaNameRequest;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateFursonaNameUseCase implements UseCase<UpdateFursonaNameUseCase.Input, Boolean> {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final UpdateFursonaNameAction action;
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final GeneralChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull UpdateFursonaNameUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        Long targetUserId = input.req.getUserId();

        boolean isAdmin = false;
        if (targetUserId != null) {
            isAdmin = permissionFinder.userHasAnyPermission(
                    requesterUserId,
                    Permission.CAN_MANAGE_USER_PUBLIC_INFO,
                    Permission.CAN_PERFORM_CHECKINS
            );
        }
        long userId = checks.getUserIdAssertPermissionCheckTimeframe(
                targetUserId,
                input.user,
                null,
                badgeConfig.getEditingDeadline(),
                input.event,
                isAdmin
        );

        log.info("User {} is updating fursona name of {} to '{}'", requesterUserId, userId, input.req.getFursonaName());

        return action.invoke(
                userId,
                input.req.getFursonaName()
        );
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull UpdateFursonaNameRequest req,
            @NotNull Event event
    ) {}
}
