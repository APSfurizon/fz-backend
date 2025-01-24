package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class NosecountRoomType {
    @NotNull private final RoomData roomData;
    @NotNull private final List<NosecountRoom> rooms;
}
