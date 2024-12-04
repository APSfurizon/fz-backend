package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomDataResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Orders.ORDERS;
import static net.furizon.jooq.generated.tables.Rooms.ROOMS;

public class JooqRoomInfoMapper {
    @NotNull
    public static RoomInfo map(Record record, PretixInformation pretixInformation) {
        short roomCapacity = record.get(ORDERS.ORDER_ROOM_CAPACITY);
        return RoomInfo.builder()
                .roomId(record.get(ROOMS.ROOM_ID))
                .roomName(record.get(ROOMS.ROOM_NAME))
                .roomOwnerId(record.get(ORDERS.USER_ID))
                .confirmed(record.get(ROOMS.ROOM_CONFIRMED))
                .roomData(
                        new RoomDataResponse(
                                roomCapacity,
                                pretixInformation.getRoomNamesFromInfo(
                                        record.get(ORDERS.ORDER_HOTEL_INTERNAL_NAME),
                                        roomCapacity
                                )
                        )
                )
            .build();
    }
}
