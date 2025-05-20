package net.furizon.backend.feature.fursuits.mapper;

import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayDataWithUserIdOrderCodeAndSerial;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.FURSUITS;
import static net.furizon.jooq.generated.Tables.ORDERS;

public class JooqFursuitDisplayMapper {

    @NotNull
    public static FursuitDisplayData mapWithOrder(Record record, boolean isOwner) {
        return map(record, true, isOwner);
    }
    @NotNull
    public static FursuitDisplayData map(Record record, boolean isOwner) {
        return map(record, false, isOwner);
    }

    @NotNull
    public static FursuitDisplayData map(Record record, boolean mapOrder, boolean isOwner) {
        Short sponsor = mapOrder ? record.get(ORDERS.ORDER_SPONSORSHIP_TYPE) : null;
        return FursuitDisplayData.builder()
                .id(record.get(FURSUITS.FURSUIT_ID))
                .name(record.get(FURSUITS.FURSUIT_NAME))
                .species(record.get(FURSUITS.FURSUIT_SPECIES))
                .propic(MediaResponseMapper.mapOrNull(record))
                .ownerId((isOwner || record.get(FURSUITS.SHOW_OWNER)) ? record.get(FURSUITS.USER_ID) : null)
                .sponsorship(mapOrder && sponsor != null ? Sponsorship.get(sponsor) : null)
            .build();
    }

    @NotNull
    public static FursuitDisplayDataWithUserIdOrderCodeAndSerial mapWithUserIdOrderCodeSerial(
            Record record,
            boolean isOwner) {
        return new FursuitDisplayDataWithUserIdOrderCodeAndSerial(
                mapWithOrder(record, isOwner),
                record.get(FURSUITS.USER_ID),
                record.get(ORDERS.ORDER_CODE),
                record.get(ORDERS.ORDER_SERIAL_IN_EVENT)
        );
    }
}
