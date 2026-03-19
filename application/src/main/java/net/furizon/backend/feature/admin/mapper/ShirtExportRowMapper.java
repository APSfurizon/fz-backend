package net.furizon.backend.feature.admin.mapper;

import net.furizon.backend.feature.admin.dto.ShirtExportRow;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;
import static net.furizon.jooq.generated.Tables.ORDERS;

public class ShirtExportRowMapper {
    public static @NotNull ShirtExportRow map(@NotNull Record r) {
        return ShirtExportRow.builder()
                .orderCode(r.get(ORDERS.ORDER_CODE))
                .orderSerial(r.get(ORDERS.ORDER_SERIAL_IN_EVENT))
                .sponsorshipType(Sponsorship.getFromDbId(r.get(ORDERS.ORDER_SPONSORSHIP_TYPE)))
                .firstName(r.get(MEMBERSHIP_INFO.INFO_FIRST_NAME))
                .lastName(r.get(MEMBERSHIP_INFO.INFO_LAST_NAME))
                .shirtSize(r.get(MEMBERSHIP_INFO.INFO_SHIRT_SIZE))
                .userId(r.get(MEMBERSHIP_INFO.USER_ID))
                .email(r.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL))
            .build();
    }
}
