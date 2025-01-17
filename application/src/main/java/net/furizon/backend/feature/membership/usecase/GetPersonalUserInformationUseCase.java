package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetPersonalUserInformationUseCase implements UseCase<FurizonUser, PersonalUserInformation> {
    @NotNull private final PersonalInfoFinder personalInfoFinder;

    @Override
    public @NotNull PersonalUserInformation executor(@NotNull FurizonUser input) {
        return Objects.requireNonNull(personalInfoFinder.findByUserId(input.getUserId()));
    }
}
