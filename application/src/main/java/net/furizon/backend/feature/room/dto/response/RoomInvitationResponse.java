package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomInfo;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class RoomInvitationResponse {
    @NotNull private final RoomInfo room;

    @NotNull private final RoomGuestResponse invitation;
}
