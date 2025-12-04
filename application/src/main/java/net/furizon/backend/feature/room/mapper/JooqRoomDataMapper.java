package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Map;

import static net.furizon.jooq.generated.Tables.ORDERS;

public class JooqRoomDataMapper {
    @NotNull
    public static RoomData map(Record record, PretixInformation pretixInformation) {
        Long roomItemId = record.get(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID);
        return new RoomData(
                record.get(ORDERS.ORDER_ROOM_CAPACITY),
                roomItemId,
                record.get(ORDERS.ORDER_ROOM_INTERNAL_NAME),
                roomItemId == null ? Map.of() : pretixInformation.getItemNames(roomItemId)
        );
    }
}
