package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.action.updateUserBadge.UpdateUserBadgeAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.feature.badge.dto.UpdateUserBadgeRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserBadgeInfoUseCase implements UseCase<UpdateUserBadgeInfoUseCase.Input, Boolean> {
    @NotNull private final UpdateUserBadgeAction action;
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final GeneralChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        Long targetUserId = input.req.getUserId();


        long userId = checks.getUserIdAssertPermissionCheckTimeframe(
                targetUserId,
                input.user,
                Permission.CAN_MANAGE_USER_PUBLIC_INFO,
                badgeConfig.getEditingDeadline(),
                input.event
        );
        UpdateUserBadgeRequest req = input.req;


        log.info("User {} is updating badge to {}. Badge info: {}", requesterUserId, userId, req);

        return action.invoke(
                userId,
                req.getFursonaName(),
                req.getLocale()
        );
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull UpdateUserBadgeRequest req,
            @NotNull Event event
    ) {}
}
