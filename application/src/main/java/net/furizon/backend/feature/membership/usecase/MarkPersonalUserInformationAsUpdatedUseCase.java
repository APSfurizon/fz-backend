package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.markInfoAsUpdated.MarkPersonalUserInformationAsUpdated;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkPersonalUserInformationAsUpdatedUseCase implements
        UseCase<MarkPersonalUserInformationAsUpdatedUseCase.Input, Boolean> {
    @NotNull private final MarkPersonalUserInformationAsUpdated markPersonalUserInformationAsUpdated;

    @Override
    public @NotNull Boolean executor(@NotNull MarkPersonalUserInformationAsUpdatedUseCase.Input input) {
        log.info("User {} is marking his information as already up to date", input.userId);
        markPersonalUserInformationAsUpdated.invoke(input.userId, input.event);
        return true;
    }

    public record Input(long userId, @NotNull Event event){}
}
