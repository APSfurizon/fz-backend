package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Orders.ORDERS;

public class JooqRoomDataMapper {
    @NotNull
    public static RoomData map(Record record, PretixInformation pretixInformation) {
        return new RoomData(
                record.get(ORDERS.ORDER_ROOM_CAPACITY),
                pretixInformation.getRoomNamesFromRoomPretixItemId(
                        record.get(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID)
                )
        );
    }
}
