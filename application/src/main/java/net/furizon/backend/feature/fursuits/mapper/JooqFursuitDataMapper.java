package net.furizon.backend.feature.fursuits.mapper;

import net.furizon.backend.feature.fursuits.dto.FursuitData;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.FURSUITS;
import static net.furizon.jooq.generated.Tables.ORDERS;

public class JooqFursuitDataMapper {
    @NotNull
    public static FursuitData mapWithOrder(Record record, boolean isOwner) {
        return map(record, true, isOwner);
    }
    @NotNull
    public static FursuitData map(Record record, boolean isOwner) {
        return map(record, false, isOwner);
    }

    @NotNull
    public static FursuitData map(Record record, boolean mapOrder, boolean isOwner) {
        return FursuitData.builder()
                .bringingToEvent(mapOrder ? record.get(ORDERS.ID) != null : false)
                .ownerId(record.get(FURSUITS.USER_ID))
                .showInFursuitCount(record.get(FURSUITS.SHOW_IN_FURSUITCOUNT))
                .showOwner(record.get(FURSUITS.SHOW_OWNER))
                .fursuit(JooqFursuitDisplayMapper.map(record, mapOrder, isOwner))
                .build();
    }
}
