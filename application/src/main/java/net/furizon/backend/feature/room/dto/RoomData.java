package net.furizon.backend.feature.room.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
@AllArgsConstructor
public class RoomData implements Comparable<RoomData> {
    private short roomCapacity;
    @Nullable private Long roomPretixItemId;
    @Nullable private String roomInternalName;
    @NotNull private Map<String, String> roomTypeNames;

    @Override
    public int compareTo(@NotNull RoomData o) {
        int v = this.roomCapacity - o.roomCapacity;
        if (v == 0 && roomPretixItemId != null && o.roomPretixItemId != null) {
            v = roomPretixItemId.compareTo(o.roomPretixItemId);
        }
        return v;
    }
}
