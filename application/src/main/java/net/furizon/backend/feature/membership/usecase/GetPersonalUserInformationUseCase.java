package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetPersonalUserInformationUseCase implements UseCase<FurizonUser, PersonalUserInformation> {
    @NotNull private final PersonalInfoFinder personalInfoFinder;
    @NotNull private final GeneralChecks generalChecks;

    @Override
    public @NotNull PersonalUserInformation executor(@NotNull FurizonUser input) {
        var pui = personalInfoFinder.findByUserId(input.getUserId());
        return generalChecks.assertUserFound(pui);
    }
}
