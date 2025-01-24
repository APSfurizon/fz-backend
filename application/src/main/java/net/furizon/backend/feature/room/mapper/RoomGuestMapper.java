package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.RoomGuest;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.ROOM_GUESTS;

public class RoomGuestMapper {
    @NotNull
    public static RoomGuest map(Record record) {
        return new RoomGuest(
                record.get(ROOM_GUESTS.ROOM_GUEST_ID),
                record.get(ROOM_GUESTS.USER_ID),
                record.get(ROOM_GUESTS.ROOM_ID),
                record.get(ROOM_GUESTS.CONFIRMED)
        );
    }
}
