package net.furizon.backend.feature.pretix.objects.checkins.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.CheckinType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class RedeemCheckinRequest {
    @NotNull
    private final List<Long> checkinListIds;

    @Nullable
    //Defaults to ENTRY
    private final CheckinType checkinType;

    @NotNull
    private final String secret;
}
