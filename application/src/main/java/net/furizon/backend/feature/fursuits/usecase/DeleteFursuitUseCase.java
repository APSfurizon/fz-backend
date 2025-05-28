package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.deleteFursuit.DeleteFursuitAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteFursuitUseCase implements UseCase<DeleteFursuitUseCase.Input, Boolean> {
    @NotNull private final DeleteFursuitAction deleteFursuitAction;
    @NotNull private final FursuitChecks fursuitChecks;
    @NotNull private final BadgeConfig badgeConfig;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        long fursuitId = input.fursuitId;

        log.info("User {} is deleting fursuit {}", userId, fursuitId);

        fursuitChecks.assertPermissionAndTimeframe(userId, fursuitId, input.event, badgeConfig.getEditingDeadline());

        return deleteFursuitAction.invoke(fursuitId);
    }

    public record Input(
            @NotNull FurizonUser user,
            long fursuitId,
            @NotNull Event event
    ) {}
}
