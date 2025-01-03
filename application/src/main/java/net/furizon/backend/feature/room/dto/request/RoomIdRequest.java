package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class RoomIdRequest {
    @Nullable
    private final Long roomId;
}
