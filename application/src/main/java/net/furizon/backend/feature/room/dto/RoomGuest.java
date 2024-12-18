package net.furizon.backend.feature.room.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomGuest {
    private final long guestId;
    private final long userId;
    private final long roomId;
    private final boolean confirmed;
}
