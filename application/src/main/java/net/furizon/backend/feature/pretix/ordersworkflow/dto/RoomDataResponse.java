package net.furizon.backend.feature.pretix.ordersworkflow.dto;

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
