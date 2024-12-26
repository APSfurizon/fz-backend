package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class BuyUpgradeRoomRequest {
    @Nullable
    private final Long userId;

    private final long roomPretixItemId;
}
