package net.furizon.backend.feature.membership.action.updateMembershipInfo;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;

@Component
@RequiredArgsConstructor
public class JooqUpdateMembershipInfoAction implements UpdateMembershipInfoAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public void invoke(long userId, @NotNull PersonalUserInformation personalUserInformation, @Nullable Event event) {
        Long eventId = event == null ? null : event.getId();

        sqlCommand.execute(
            PostgresDSL
                    .update(MEMBERSHIP_INFO)
                    .set(MEMBERSHIP_INFO.INFO_FIRST_NAME, personalUserInformation.getFirstName())
                    .set(MEMBERSHIP_INFO.INFO_LAST_NAME, personalUserInformation.getLastName())
                    .set(MEMBERSHIP_INFO.INFO_FISCAL_CODE, personalUserInformation.getFiscalCode())
                    .set(MEMBERSHIP_INFO.INFO_BIRTH_CITY, personalUserInformation.getBirthCity())
                    .set(MEMBERSHIP_INFO.INFO_BIRTH_REGION, personalUserInformation.getBirthRegion())
                    .set(MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY, personalUserInformation.getBirthCountry())
                    .set(MEMBERSHIP_INFO.INFO_BIRTHDAY, personalUserInformation.getBirthday())
                    .set(MEMBERSHIP_INFO.INFO_ADDRESS, personalUserInformation.getResidenceAddress())
                    .set(MEMBERSHIP_INFO.INFO_ZIP, personalUserInformation.getResidenceZipCode())
                    .set(MEMBERSHIP_INFO.INFO_CITY, personalUserInformation.getResidenceCity())
                    .set(MEMBERSHIP_INFO.INFO_REGION, personalUserInformation.getResidenceRegion())
                    .set(MEMBERSHIP_INFO.INFO_COUNTRY, personalUserInformation.getResidenceCountry())
                    .set(MEMBERSHIP_INFO.INFO_PHONE, personalUserInformation.getPhoneNumber())
                    .set(MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID, eventId)
                .where(MEMBERSHIP_INFO.USER_ID.eq(userId))
        );
    }
}
