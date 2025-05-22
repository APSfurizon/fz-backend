package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class RoomInfoResponse {
    @Nullable
    private final RoomInfo currentRoomInfo;

    private boolean hasOrder;
    private boolean canCreateRoom;
    private boolean buyOrUpgradeRoomSupported;
    private boolean canBuyOrUpgradeRoom;
    private boolean canExchange;

    @Nullable
    private final OffsetDateTime editingRoomEndTime;

    @NotNull
    private final List<RoomInvitationResponse> invitations;
}
