package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class NosecountRoom {
    private final long id;
    @NotNull private final String roomName;
    @NotNull private final List<UserDisplayData> guests;
}
