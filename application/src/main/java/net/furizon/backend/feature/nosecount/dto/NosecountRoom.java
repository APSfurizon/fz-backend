package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithExtraDays;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class NosecountRoom {
    private final long id;
    private final long ownerUserId;
    @NotNull private final String roomName;
    @NotNull private final List<UserDisplayDataWithExtraDays> guests;
    @Nullable private ExtraDays roomExtraDays;
}
