package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.dto.requests.RegisterUserRequest;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserValidation {
    private final SessionAuthenticationManager sessionAuthenticationManager;
    @Lazy
    private final PretixInformation pretixInformation;

    public void validate(@NotNull RegisterUserRequest input) {
        final var authentication = sessionAuthenticationManager.findAuthenticationByEmail(input.getEmail());
        if (authentication != null) {
            throw new ApiException(
                "User already exists with email: %s".formatted(input.getEmail()),
                AuthenticationCodes.EMAIL_ALREADY_REGISTERED.name()
            );
        }

        final var birthCountryCode = input.getPersonalUserInformation().getBirthCountry();
        if (input.getPersonalUserInformation().getBirthRegion() == null
                && pretixInformation.getStatesOfCountry(birthCountryCode).size() > 0) {
            throw new ApiException(
                "Region not provided for: %s".formatted(birthCountryCode),
                AuthenticationCodes.REGION_NOT_PROVIDED.name()
            );
        }

        final var residenceCountryCode = input.getPersonalUserInformation().getResidenceCountry();
        if (input.getPersonalUserInformation().getResidenceRegion() == null
                && pretixInformation.getStatesOfCountry(residenceCountryCode).size() > 0) {
            throw new ApiException(
                    "Region not provided for: %s".formatted(residenceCountryCode),
                    AuthenticationCodes.REGION_NOT_PROVIDED.name()
            );
        }
    }
}
