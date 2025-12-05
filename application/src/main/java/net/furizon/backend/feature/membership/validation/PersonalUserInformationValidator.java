package net.furizon.backend.feature.membership.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonalUserInformationValidator {
    @Lazy
    private final PretixInformation pretixInformation;
    @NotNull TranslationService translationService;

    public void validate(@NotNull PersonalUserInformation userInfo) {

        String birthCountry = userInfo.getBirthCountry();
        String birthRegion = userInfo.getBirthRegion();
        boolean birthCountryHasRegions = pretixInformation.getStatesOfCountry(birthCountry).size() > 0;

        if (!pretixInformation.isCountryValid(birthCountry)) {
            throw new ApiException(
                translationService.error("user.personal_information.invalid_birth_country", birthCountry),
                AuthenticationCodes.COUNTRY_INVALID
            );
        }
        if (birthCountryHasRegions) {
            if (birthRegion == null) {
                throw new ApiException(
                    translationService.error("user.personal_information.expected_birth_region", birthCountry),
                    AuthenticationCodes.REGION_NOT_PROVIDED
                );
            }
            if (!pretixInformation.isRegionOfCountryValid(birthCountry, birthRegion)) {
                throw new ApiException(
                    translationService.error("user.personal_information.invalid_birth_region", birthRegion),
                    AuthenticationCodes.REGION_INVALID
                );
            }
        }

        String residenceCountry = userInfo.getResidenceCountry();
        String residenceRegion = userInfo.getResidenceRegion();
        boolean residenceCountryHasRegions = pretixInformation.getStatesOfCountry(residenceCountry).size() > 0;

        if (!pretixInformation.isCountryValid(residenceCountry)) {
            throw new ApiException(
                translationService.error("user.personal_information.invalid_residence_country", residenceCountry),
                AuthenticationCodes.COUNTRY_INVALID
            );
        }
        if (residenceCountryHasRegions) {
            if (residenceRegion == null) {
                throw new ApiException(
                    translationService.error("user.personal_information.expected_residence_region", residenceCountry),
                    AuthenticationCodes.REGION_NOT_PROVIDED
                );
            }
            if (!pretixInformation.isRegionOfCountryValid(residenceCountry, residenceRegion)) {
                throw new ApiException(
                    translationService.error("user.personal_information.invalid_residence_region", residenceRegion),
                    AuthenticationCodes.REGION_INVALID
                );
            }
        }

        String phonePrefix = userInfo.getPrefixPhoneNumber();
        if (!pretixInformation.isPhonePrefixValid(phonePrefix)) {
            throw new ApiException(
                translationService.error("user.personal_information.invalid_phone_prefix", phonePrefix),
                AuthenticationCodes.PHONE_PREFIX_INVALID
            );
        }

        if (userInfo.getTelegramUsername().isBlank()) {
            throw new ApiException(
                translationService.error("user.personal_information.invalid_telegram_username"),
                AuthenticationCodes.TELEGRAM_USERNAME_EMPTY
            );
        }

        if (userInfo.getIdNumber().isBlank()) {
            throw new ApiException(
                translationService.error("user.personal_information.invalid_id_number"),
                AuthenticationCodes.ID_NUMBER_EMPTY
            );
        }

        if (userInfo.getIdIssuer().isBlank()) {
            throw new ApiException(
                translationService.error("user.personal_information.invalid_id_issuer"),
                AuthenticationCodes.ID_ISSUER_EMPTY
            );
        }

        // We don't want to check when an user inserts it's documents if it's expired or not:
        // Let's say the registrations for an event open soon and the user has not got yet a
        // new document after the previous one has expired: We don't want to stop the user from joining.
        // He will receive anyway manual mails regarding the status of his document
        /*if (userInfo.getIdExpiry().isBefore(LocalDate.now())) {
            throw new ApiException(
                    "Id Expired",
                    AuthenticationCodes.ID_EXPIRED
            );
        }*/
    }
}
