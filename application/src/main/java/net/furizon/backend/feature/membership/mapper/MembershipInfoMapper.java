package net.furizon.backend.feature.membership.mapper;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.MembershipInfo.MEMBERSHIP_INFO;

public class MembershipInfoMapper {
    @NotNull
    public static PersonalUserInformation map(Record record) {
        return PersonalUserInformation.builder()
                .id(record.getValue(MEMBERSHIP_INFO.ID))
                .firstName(record.get(MEMBERSHIP_INFO.INFO_FIRST_NAME))
                .lastName(record.get(MEMBERSHIP_INFO.INFO_LAST_NAME))
                .fiscalCode(record.get(MEMBERSHIP_INFO.INFO_FISCAL_CODE))
                .birthCity(record.get(MEMBERSHIP_INFO.INFO_BIRTH_CITY))
                .birthRegion(record.get(MEMBERSHIP_INFO.INFO_BIRTH_REGION))
                .birthday(record.get(MEMBERSHIP_INFO.INFO_BIRTHDAY))
                .residenceAddress(record.get(MEMBERSHIP_INFO.INFO_ADDRESS))
                .residenceZipCode(record.get(MEMBERSHIP_INFO.INFO_ZIP))
                .residenceCity(record.get(MEMBERSHIP_INFO.INFO_CITY))
                .residenceRegion(record.get(MEMBERSHIP_INFO.INFO_REGION))
                .residenceCountry(record.get(MEMBERSHIP_INFO.INFO_COUNTRY))
                .prefixPhoneNumber(record.get(MEMBERSHIP_INFO.INFO_PHONE_PREFIX))
                .phoneNumber(record.get(MEMBERSHIP_INFO.INFO_PHONE))
                .lastUpdatedEventId(record.get(MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID))
                .userId(record.get(MEMBERSHIP_INFO.USER_ID))
            .build();
    }
}
