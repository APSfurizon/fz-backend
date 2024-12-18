package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUserDisplayDataUseCase implements
        UseCase<GetUserDisplayDataUseCase.Input, Optional<UserDisplayData>> {
    private final UserFinder userFinder;

    @Override
    public @NotNull Optional<UserDisplayData> executor(@NotNull GetUserDisplayDataUseCase.Input input) {
        if (input.event == null) {
            log.error("Event is null!");
            return Optional.empty();
        }

        return Optional.ofNullable(userFinder.getDisplayUser(input.userId, input.event));
    }

    public record Input(long userId, @NotNull Event event) {}
}
