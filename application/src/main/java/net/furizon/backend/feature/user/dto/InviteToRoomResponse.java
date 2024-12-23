package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomGuest;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Data
public class InviteToRoomResponse {
    @NotNull
    Set<RoomGuest> guests;
}
