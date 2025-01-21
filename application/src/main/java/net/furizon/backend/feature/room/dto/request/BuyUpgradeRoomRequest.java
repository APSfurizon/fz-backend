package net.furizon.backend.feature.room.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyUpgradeRoomRequest {
    @Nullable
    private final Long userId;

    @NotNull
    private final Long roomPretixItemId;
}
