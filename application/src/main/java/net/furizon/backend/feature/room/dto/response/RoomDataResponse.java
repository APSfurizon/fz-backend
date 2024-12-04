package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
@AllArgsConstructor
public class RoomDataResponse {
    private short roomCapacity;
    @Nullable
    private Map<String, String> roomTypeNames;
}
