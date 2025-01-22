package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class JooqNosecountObj {
    private final long userId;
    @NotNull private final String fursonaName;
    @Nullable private final MediaResponse media;
    @NotNull private final Sponsorship sponsorship;
    private final long dailyDays;

    @Nullable private final Long roomId;
    @Nullable private final String roomName;
    @Nullable private final Short roomCapacity;
    @Nullable private final Long roomPretixItemId;
    @Nullable private final String roomInternalName;
    @Nullable private final String hotelInternalName;
}
