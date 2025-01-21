package net.furizon.backend.feature.fursuits.mapper;

import net.furizon.backend.feature.fursuits.dto.FursuitData;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;
import static net.furizon.jooq.generated.tables.Orders.ORDERS;

public class JooqFursuitDataMapper {
    @NotNull
    public static FursuitData mapWithOrder(org.jooq.Record record) {
        return map(record, true);
    }
    @NotNull
    public static FursuitData map(Record record) {
        return map(record, false);
    }

    @NotNull
    public static FursuitData map(Record record, boolean mapOrder) {
        return FursuitData.builder()
                .bringingToEvent(mapOrder ? record.get(ORDERS.ID) != null : false)
                .ownerId(record.get(FURSUITS.USER_ID))
                .showInFursuitCount(record.get(FURSUITS.SHOW_IN_FURSUITCOUNT))
                .fursuit(JooqFursuitDisplayMapper.map(record, mapOrder))
                .build();
    }
}
