package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class PretixCheckinHistory {
    @NotNull
    private final Long id;

    @NotNull
    private final Boolean successful;

    @Nullable
    @JsonProperty("error_reason")
    private final CheckinErrorCodes errorReason;
    @Nullable
    @JsonProperty("error_explanation")
    private final String errorExplanation;

    @Nullable
    @JsonProperty("position")
    private final Long positionId;

    @NotNull
    private final OffsetDateTime datetime;

    @NotNull
    @JsonProperty("created")
    private final OffsetDateTime createdAt;

    @NotNull
    @JsonProperty("list")
    private final Long checkinListId;

    @NotNull
    @JsonProperty("auto_checked_in")
    private final Boolean autoCheckedIn;

    @Nullable
    private final Long gate;
    @Nullable
    private final Long device;
    @Nullable
    @JsonProperty("device_id")
    private final Long deviceId;

    @NotNull
    private final CheckinType type;
}
