package net.furizon.backend.feature.fursuits.mapper;

import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;
import static net.furizon.jooq.generated.tables.Orders.ORDERS;

public class JooqFursuitDisplayMapper {

    @NotNull
    public static FursuitDisplayData mapWithOrder(Record record) {
        return map(record, true);
    }
    @NotNull
    public static FursuitDisplayData map(Record record) {
        return map(record, false);
    }

    @NotNull
    public static FursuitDisplayData map(Record record, boolean mapOrder) {
        return FursuitDisplayData.builder()
                .id(record.get(FURSUITS.FURSUIT_ID))
                .name(record.get(FURSUITS.FURSUIT_NAME))
                .species(record.get(FURSUITS.FURSUIT_SPECIES))
                .propic(MediaResponseMapper.mapOrNull(record))
                .bringingToEvent(mapOrder ? record.get(ORDERS.ID) != null : false)
                .ownerId(record.get(FURSUITS.USER_ID))
                .sponsorship(mapOrder ? Sponsorship.get(record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)) : null)
            .build();
    }
}
