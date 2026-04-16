package net.furizon.backend.feature.pretix.objects.checkins.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CancelCheckinRequest {
    @NotEmpty
    private final String nonce;
    @NotNull
    private final List<Long> checkinListIds;

    @Nullable
    private final String explanation;
}
