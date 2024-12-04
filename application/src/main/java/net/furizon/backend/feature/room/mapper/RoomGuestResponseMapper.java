package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.RoomGuests.ROOM_GUESTS;

public class RoomGuestResponseMapper {
    @NotNull
    public static RoomGuestResponse map(Record record) {
        return new RoomGuestResponse(
                record.get(ROOM_GUESTS.ROOM_GUEST_ID),
                record.get(ROOM_GUESTS.USER_ID),
                record.get(ROOM_GUESTS.ROOM_ID),
                record.get(ROOM_GUESTS.CONFIRMED)
        );
    }
}
