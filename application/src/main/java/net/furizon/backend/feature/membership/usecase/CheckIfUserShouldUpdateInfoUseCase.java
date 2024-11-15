package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CheckIfUserShouldUpdateInfoUseCase implements UseCase<FurizonUser, Boolean> {
    private final PersonalInfoFinder personalInfoFinder;
    private final PretixInformation pretixService;

    @Transactional
    @Override
    public @NotNull Boolean executor(@NotNull FurizonUser user) {
        PersonalUserInformation info = personalInfoFinder.findByUserId(user.getUserId());
        if (info == null) {
            return true;
        }

        var e = pretixService.getCurrentEvent();
        if (!e.isPresent()) {
            return false;
        }

        return e.get().getId() != info.getLastUpdatedEventId();
    }
}