package net.furizon.backend.feature.room.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
@AllArgsConstructor
public class RoomData {
    private short roomCapacity;
    @Nullable private Long roomPretixItemId;
    @Nullable private String roomInternalName;
    @NotNull private Map<String, String> roomTypeNames;
}
