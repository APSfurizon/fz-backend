package net.furizon.backend.feature.pretix.objects.checkins.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RedeemCheckinRequest {
    @NotNull
    private final List<Long> checkinListIds;

    @NotNull
    private final String secret;
}
