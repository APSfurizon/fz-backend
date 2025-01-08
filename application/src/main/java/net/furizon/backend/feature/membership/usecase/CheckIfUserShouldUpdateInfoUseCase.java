package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CheckIfUserShouldUpdateInfoUseCase implements UseCase<CheckIfUserShouldUpdateInfoUseCase.Input, Boolean> {
    private final PersonalInfoFinder personalInfoFinder;

    @Transactional
    @Override
    public @NotNull Boolean executor(@NotNull CheckIfUserShouldUpdateInfoUseCase.Input input) {
        FurizonUser user = input.user;
        PersonalUserInformation info = personalInfoFinder.findByUserId(user.getUserId());
        if (info == null) {
            return true;
        }

        Event e = input.event;
        return e.getId() != info.getLastUpdatedEventId();
    }

    public record Input(@NotNull FurizonUser user, @NotNull Event event) {}
}