package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.action.updateUserBadge.UpdateUserBadgeAction;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.feature.badge.dto.UpdateUserBadgeRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserBadgeInfoUseCase implements UseCase<UpdateUserBadgeInfoUseCase.Input, Boolean> {
    @NotNull private final UpdateUserBadgeAction action;
    @NotNull private final GeneralChecks checks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        UpdateUserBadgeRequest req = input.req;
        long userId = checks.getUserIdAndAssertPermission(requesterUserId, input.user);

        log.info("User {} is updating badge to {}. Badge info: {}", requesterUserId, userId, req);

        return action.invoke(
                userId,
                req.getFursonaName(),
                req.getLocale()
        );
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull UpdateUserBadgeRequest req
    ) {}
}
