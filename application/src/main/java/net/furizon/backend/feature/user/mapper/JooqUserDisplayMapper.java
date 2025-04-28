package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.membership.mapper.MembershipInfoMapper;
import net.furizon.backend.feature.user.dto.UserAdminViewDisplay;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithOrderCode;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithOrderCodeAndSerial;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithPersonalInfo;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqUserDisplayMapper {
    @NotNull
    public static UserDisplayData map(Record record) {
        return map(record, true);
    }

    @NotNull
    public static UserDisplayData map(Record record, boolean withOrder) {
        Short sponsor = withOrder ? record.get(ORDERS.ORDER_SPONSORSHIP_TYPE) : null;
        return UserDisplayData.builder()
                .userId(record.get(USERS.USER_ID))
                .fursonaName(record.get(USERS.USER_FURSONA_NAME))
                .locale(record.get(USERS.USER_LOCALE))
                .propic(MediaResponseMapper.mapOrNull(record))
                .sponsorship(sponsor != null ? Sponsorship.get(sponsor) : null)
            .build();
    }

    public static UserDisplayDataWithOrderCode mapWithOrderCode(Record record) {
        return new UserDisplayDataWithOrderCode(
                map(record, true),
                record.get(ORDERS.ORDER_CODE)
        );
    }

    public static UserDisplayDataWithOrderCodeAndSerial mapWithOrderCodeSerial(Record record) {
        return new UserDisplayDataWithOrderCodeAndSerial(
                map(record, true),
                record.get(ORDERS.ORDER_CODE),
                record.get(ORDERS.ORDER_SERIAL_IN_EVENT)
        );
    }

    public static UserDisplayDataWithPersonalInfo mapWithOrderAndPersonalInfo(Record record) {
        return new UserDisplayDataWithPersonalInfo(
            mapWithOrderCode(record),
            record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL),
            MembershipInfoMapper.map(record)
        );
    }

    public static UserAdminViewDisplay mapWithAdminView(Record record) {
        return new UserAdminViewDisplay(
                map(record),
                record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL),
                MembershipInfoMapper.map(record),
                record.get(AUTHENTICATIONS.AUTHENTICATION_DISABLED)
        );
    }
}
