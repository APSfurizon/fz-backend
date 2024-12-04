package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomGuestResponse {
    private final long guestId;
    private final long userId;
    private final long roomId;
    private final boolean confirmed;
}
