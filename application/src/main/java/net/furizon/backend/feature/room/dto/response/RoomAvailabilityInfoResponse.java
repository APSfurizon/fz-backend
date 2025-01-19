package net.furizon.backend.feature.room.dto.response;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuotaAvailability;
import net.furizon.backend.feature.room.dto.RoomData;
import org.jetbrains.annotations.NotNull;

@Data
public class RoomAvailabilityInfoResponse {
    @NotNull
    private final RoomData data;
    @NotNull
    private final String price;
    private final long remaining;
}
