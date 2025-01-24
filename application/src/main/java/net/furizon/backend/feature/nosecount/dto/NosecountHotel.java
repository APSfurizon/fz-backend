package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Data
public class NosecountHotel {
    @NotNull private final Map<String, String> displayName;
    @NotNull private final String internalName;

    @NotNull private final List<NosecountRoomType> roomTypes;
}
