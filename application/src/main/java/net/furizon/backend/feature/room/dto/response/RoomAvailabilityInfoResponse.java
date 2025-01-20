package net.furizon.backend.feature.room.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.feature.room.dto.RoomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class RoomAvailabilityInfoResponse {
    @NotNull
    private final RoomData data;
    @NotNull
    private final String price;
    @Nullable
    private final Long remaining;
}
