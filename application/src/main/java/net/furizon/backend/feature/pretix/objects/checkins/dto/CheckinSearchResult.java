package net.furizon.backend.feature.pretix.objects.checkins.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class CheckinSearchResult {
    @NotNull private final String name;
    @NotNull private final String orderCode;
    @Nullable private final String checkinSecret;

    private boolean hasCheckedIn;

    @NotNull private UserDisplayData user;
}
