package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class PretixPositionCheckin {
    @NotNull
    private final Long id;

    @NotNull
    @JsonProperty("list")
    private final Long checkinListId;

    @NotNull
    @JsonProperty("datetime")
    private final OffsetDateTime checkinDatetime;

    @Nullable
    private final String type;

    @Nullable
    private final Long gate;

    @Nullable
    @JsonProperty("device_id")
    private final Long deviceId;

    @NotNull
    @JsonProperty("auto_checked_in")
    private final Boolean autoCheckedIn;
}
