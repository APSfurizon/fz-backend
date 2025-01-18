package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.FursuitErrorCodes;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSingleFursuitUseCase implements UseCase<GetSingleFursuitUseCase.Input, FursuitDisplayData> {
    @NotNull private final FursuitFinder fursuitFinder;

    @Override
    public @NotNull FursuitDisplayData executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        Event event = input.event;

        FursuitDisplayData fursuit = fursuitFinder.getFursuit(userId, event);
        if (fursuit == null) {
            throw new ApiException("Fursuit not found", FursuitErrorCodes.FURSUIT_NOT_FOUND);
        }

        if (!fursuit.isBringingToEvent()) {
            //TODO check if user has permimssion on fursuit
        }

        return fursuit;
    }

    public record Input(
            long fursuitId,
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
