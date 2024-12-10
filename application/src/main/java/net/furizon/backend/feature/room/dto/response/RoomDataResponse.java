package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
@AllArgsConstructor
public class RoomDataResponse {
    private short roomCapacity;
    //@NotNull //This shouldn't be needed
    //private String hotelInternalName;
    @NotNull
    private Map<String, String> roomTypeNames;
}
