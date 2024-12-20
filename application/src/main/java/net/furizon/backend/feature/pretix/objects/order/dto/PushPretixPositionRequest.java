package net.furizon.backend.feature.pretix.objects.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PushPretixPositionRequest {

    private final String order;

    @JsonProperty("addon_to")
    private final long addonTo;

    private final long item;
}
