package net.furizon.backend.feature.pretix.objects.order.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdatePositionBundleStatus {
    private final long position;

    @JsonProperty("is_bundle")
    private final boolean bundled;
}
