package net.furizon.backend.feature.membership.action.addMembershipInfo;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.PersonalUserInformation;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;

@Component
@RequiredArgsConstructor
public class JooqAddMembershipInfoAction implements AddMembershipInfoAction {
    private final SqlCommand sqlCommand;

    @Override
    public void invoke(long userId, @NotNull PersonalUserInformation personalUserInformation) {
        sqlCommand.execute(
            PostgresDSL
                .insertInto(
                    MEMBERSHIP_INFO,
                    MEMBERSHIP_INFO.INFO_FIRST_NAME,
                    MEMBERSHIP_INFO.INFO_LAST_NAME,
                    MEMBERSHIP_INFO.INFO_FISCAL_CODE,
                    MEMBERSHIP_INFO.INFO_BIRTH_CITY,
                    MEMBERSHIP_INFO.INFO_BIRTH_REGION,
                    MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY,
                    MEMBERSHIP_INFO.INFO_BIRTHDAY,
                    MEMBERSHIP_INFO.INFO_ADDRESS,
                    MEMBERSHIP_INFO.INFO_ZIP,
                    MEMBERSHIP_INFO.INFO_CITY,
                    MEMBERSHIP_INFO.INFO_REGION,
                    MEMBERSHIP_INFO.INFO_COUNTRY,
                    MEMBERSHIP_INFO.INFO_PHONE,
                    MEMBERSHIP_INFO.USER_ID
                )
                .values(
                    personalUserInformation.getFirstName(),
                    personalUserInformation.getLastName(),
                    personalUserInformation.getFiscalCode(),
                    personalUserInformation.getBirthCity(),
                    personalUserInformation.getBirthRegion(),
                    personalUserInformation.getBirthCountry(),
                    personalUserInformation.getBirthday(),
                    personalUserInformation.getResidenceAddress(),
                    personalUserInformation.getResidenceZipCode(),
                    personalUserInformation.getResidenceCity(),
                    personalUserInformation.getResidenceRegion(),
                    personalUserInformation.getResidenceCountry(),
                    personalUserInformation.getPhoneNumber(),
                    userId
                )
        );
    }
}
