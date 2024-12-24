package net.furizon.backend.feature.room.dto.response;

import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
public class ListRoomPricesAvailabilityResponse {
    @Nullable
    private final RoomData currentRoom;
    @Nullable
    private final String priceOfCurrentRoom;
    @NotNull
    private final List<RoomAvailabilityInfoResponse> rooms;
}
