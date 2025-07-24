package net.furizon.backend.feature.membership.action.addMembershipInfo;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;

@Component
@RequiredArgsConstructor
public class JooqAddMembershipInfoAction implements AddMembershipInfoAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public void invoke(long userId, @NotNull PersonalUserInformation personalUserInformation, @NotNull Event event) {
        sqlCommand.execute(
            PostgresDSL
                .insertInto(
                    MEMBERSHIP_INFO,
                    MEMBERSHIP_INFO.INFO_FIRST_NAME,
                    MEMBERSHIP_INFO.INFO_LAST_NAME,
                    MEMBERSHIP_INFO.INFO_FISCAL_CODE,
                    MEMBERSHIP_INFO.INFO_DOCUMENT_SEX,
                    MEMBERSHIP_INFO.INFO_GENDER,
                    MEMBERSHIP_INFO.INFO_BIRTH_CITY,
                    MEMBERSHIP_INFO.INFO_BIRTH_REGION,
                    MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY,
                    MEMBERSHIP_INFO.INFO_BIRTHDAY,
                    MEMBERSHIP_INFO.INFO_ADDRESS,
                    MEMBERSHIP_INFO.INFO_ZIP,
                    MEMBERSHIP_INFO.INFO_CITY,
                    MEMBERSHIP_INFO.INFO_REGION,
                    MEMBERSHIP_INFO.INFO_COUNTRY,
                    MEMBERSHIP_INFO.INFO_PHONE_PREFIX,
                    MEMBERSHIP_INFO.INFO_PHONE,
                    MEMBERSHIP_INFO.INFO_ALLERGIES,
                    MEMBERSHIP_INFO.INFO_TELEGRAM_USERNAME,
                    MEMBERSHIP_INFO.USER_ID,
                    MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID,
                    MEMBERSHIP_INFO.INFO_ID_TYPE,
                    MEMBERSHIP_INFO.INFO_ID_NUMBER,
                    MEMBERSHIP_INFO.INFO_ID_ISSUER,
                    MEMBERSHIP_INFO.INFO_ID_EXPIRY,
                    MEMBERSHIP_INFO.INFO_SHIRT_SIZE
                )
                .values(
                    personalUserInformation.getFirstName(),
                    personalUserInformation.getLastName(),
                    personalUserInformation.getFiscalCode(),
                    personalUserInformation.getSex(),
                    personalUserInformation.getGender(),
                    personalUserInformation.getBirthCity(),
                    personalUserInformation.getBirthRegion(),
                    personalUserInformation.getBirthCountry(),
                    personalUserInformation.getBirthday(),
                    personalUserInformation.getResidenceAddress(),
                    personalUserInformation.getResidenceZipCode(),
                    personalUserInformation.getResidenceCity(),
                    personalUserInformation.getResidenceRegion(),
                    personalUserInformation.getResidenceCountry(),
                    personalUserInformation.getPrefixPhoneNumber(),
                    personalUserInformation.getPhoneNumber(),
                    personalUserInformation.getAllergies(),
                    personalUserInformation.getTelegramUsername(),
                    userId,
                    event.getId(),
                    personalUserInformation.getIdType(),
                    personalUserInformation.getIdNumber(),
                    personalUserInformation.getIdIssuer(),
                    personalUserInformation.getIdExpiry(),
                    personalUserInformation.getShirtSize()
                )
        );
    }
}
