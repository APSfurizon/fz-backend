package net.furizon.backend.feature.pretix.objects.checkins.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.CheckinType;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
@Builder
public class CheckinHistory {
    private final long checkinId;
    private final boolean successful;

    @Nullable private final String localizedErrorReason;
    @Nullable private final String optionalErrorMessage;

    @Nullable private final String orderCode;
    @Nullable private final String firstName;
    @Nullable private final String lastName;
    @Nullable private final UserDisplayData user;

    @NotNull private final OffsetDateTime datetime;
    @NotNull private final OffsetDateTime createdAt;

    private final long checkinListId;
    private boolean autoCheckedIn;

    @NotNull private final CheckinType type;
}
