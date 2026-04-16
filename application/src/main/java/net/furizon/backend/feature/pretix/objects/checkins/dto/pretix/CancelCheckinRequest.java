package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class CancelCheckinRequest {
    @NotNull
    private final String nonce;

    @NotNull
    private final List<Long> lists;

    @Nullable
    @JsonProperty("error_explanation")
    private final String comment;
}
