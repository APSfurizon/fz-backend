package net.furizon.backend.feature.membership.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PersonalUserInformationValidator {
    @Lazy
    private final PretixInformation pretixInformation;

    public void validate(@NotNull PersonalUserInformation userInfo) {

        String birthCountry = userInfo.getBirthCountry();
        String birthRegion = userInfo.getBirthRegion();
        boolean birthCountryHasRegions = pretixInformation.getStatesOfCountry(birthCountry).size() > 0;

        if (!pretixInformation.isCountryValid(birthCountry)) {
            throw new ApiException(
                    "Invalid birth country: %s".formatted(birthCountry),
                    AuthenticationCodes.COUNTRY_INVALID
            );
        }
        if (birthCountryHasRegions) {
            if (birthRegion == null) {
                throw new ApiException(
                        "Birth region not provided for: %s".formatted(birthCountry),
                        AuthenticationCodes.REGION_NOT_PROVIDED
                );
            }
            if (!pretixInformation.isRegionOfCountryValid(birthCountry, birthRegion)) {
                throw new ApiException(
                        "Invalid birth region: %s".formatted(birthRegion),
                        AuthenticationCodes.REGION_INVALID
                );
            }
        }

        String residenceCountry = userInfo.getResidenceCountry();
        String residenceRegion = userInfo.getResidenceRegion();
        boolean residenceCountryHasRegions = pretixInformation.getStatesOfCountry(residenceCountry).size() > 0;

        if (!pretixInformation.isCountryValid(residenceCountry)) {
            throw new ApiException(
                    "Invalid residence country: %s".formatted(residenceCountry),
                    AuthenticationCodes.COUNTRY_INVALID
            );
        }
        if (residenceCountryHasRegions) {
            if (residenceRegion == null) {
                throw new ApiException(
                        "Residence region not provided for: %s".formatted(residenceCountry),
                        AuthenticationCodes.REGION_NOT_PROVIDED
                );
            }
            if (!pretixInformation.isRegionOfCountryValid(residenceCountry, residenceRegion)) {
                throw new ApiException(
                        "Invalid residence region: %s".formatted(residenceRegion),
                        AuthenticationCodes.REGION_INVALID
                );
            }
        }

        String phonePrefix = userInfo.getPrefixPhoneNumber();
        if (!pretixInformation.isPhonePrefixValid(phonePrefix)) {
            throw new ApiException(
                    "Invalid phone prefix: %s".formatted(phonePrefix),
                    AuthenticationCodes.PHONE_PREFIX_INVALID
            );
        }

        if (userInfo.getIdExpiry().isBefore(LocalDate.now())) {
            throw new ApiException(
                    "Id Expired",
                    AuthenticationCodes.ID_EXPIRED
            );
        }
    }
}
