package net.furizon.backend.feature.room.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.room.dto.response.RoomDataResponse;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class RoomInfo {
    private final long roomId;

    @Nullable
    private final String roomName;

    private final long roomOwnerId;
    @Builder.Default
    private boolean isOwner = false;

    private final boolean confirmed;

    @NotNull
    private final RoomDataResponse roomData;

    @Nullable
    @Builder.Default
    private List<RoomGuestResponse> guests = null;
}
