package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.membership.action.markInfoAsUpdated.MarkPersonalUserInformationAsUpdated;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.membership.validation.PersonalUserInformationValidator;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkPersonalUserInformationAsUpdatedUseCase implements
        UseCase<MarkPersonalUserInformationAsUpdatedUseCase.Input, Boolean> {
    @NotNull private final MarkPersonalUserInformationAsUpdated markPersonalUserInformationAsUpdated;
    @NotNull private final PersonalUserInformationValidator validator;
    @NotNull private final TranslationService translationService;
    @NotNull private final PersonalInfoFinder finder;

    @Override
    public @NotNull Boolean executor(@NotNull MarkPersonalUserInformationAsUpdatedUseCase.Input input) {
        PersonalUserInformation information = finder.findByUserId(input.userId);
        if (information == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        try {
            validator.validate(information);
        } catch (ApiException e) {
            throw new ApiException(
                translationService.error(
                    "user.personal_information.cant_mark_as_updated",
                    e.getErrors().stream().map(ApiError::getCode).collect(Collectors.joining(","))
                ),
                AuthenticationCodes.CANNOT_MARK_INFO_AS_UPDATED
            );
        }
        log.info("User {} is marking his information as already up to date", input.userId);
        markPersonalUserInformationAsUpdated.invoke(input.userId, input.event);
        return true;
    }

    public record Input(long userId, @NotNull Event event){}
}
