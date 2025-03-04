package net.furizon.backend.feature.badge.mapper;

import net.furizon.backend.feature.badge.dto.UserBadgePrint;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.HashSet;
import java.util.List;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqUserBadgePrintMapper {
    @NotNull
    public static UserBadgePrint map(Record record) {
        return UserBadgePrint.builder()
                .userId(record.get(USERS.USER_ID))
                .fursonaName(record.get(USERS.USER_FURSONA_NAME))
                .locales(List.of(record.get(USERS.USER_LOCALE))) //TODO support more flags
                .imageUrl(record.get(MEDIA.MEDIA_PATH))
                .imageMimeType(record.get(MEDIA.MEDIA_TYPE))
                .serialNo(record.get(ORDERS.ORDER_SERIAL_IN_EVENT))
                .orderCode(record.get(ORDERS.ORDER_CODE))
                .sponsorship(Sponsorship.get(record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)))
                .permissions(new HashSet<>())
            .build();
    }
}
