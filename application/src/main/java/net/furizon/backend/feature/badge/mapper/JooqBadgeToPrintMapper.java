package net.furizon.backend.feature.badge.mapper;

import net.furizon.backend.feature.badge.dto.BadgeToPrint;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.HashSet;
import java.util.List;

import static net.furizon.jooq.generated.Tables.FURSUITS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqBadgeToPrintMapper {

    @NotNull
    public static BadgeToPrint mapUser(Record record) {
        return map(record, false);
    }

    @NotNull
    public static BadgeToPrint mapFursuit(Record record) {
        return map(record, true);
    }

    @NotNull
    public static BadgeToPrint map(Record record, boolean withFursuit) {
        return BadgeToPrint.builder()
                .userId(record.get(USERS.USER_ID))
                .fursonaName(record.get(USERS.USER_FURSONA_NAME))
                .locales(List.of(record.get(USERS.USER_LOCALE))) //TODO support more flags
                .imageUrl(record.get(MEDIA.MEDIA_PATH))
                .imageMimeType(record.get(MEDIA.MEDIA_TYPE))
                .serialNo(record.get(ORDERS.ORDER_SERIAL_IN_EVENT))
                .orderCode(record.get(ORDERS.ORDER_CODE))
                .sponsorship(Sponsorship.get(record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)))
                .fursuitId(withFursuit ? record.get(FURSUITS.FURSUIT_ID) : null)
                .fursuitName(withFursuit ? record.get(FURSUITS.FURSUIT_NAME) : null)
                .fursuitSpecies(withFursuit ? record.get(FURSUITS.FURSUIT_SPECIES) : null)
                .permissions(new HashSet<>())
            .build();
    }
}
