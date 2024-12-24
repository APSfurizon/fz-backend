package net.furizon.backend.feature.pretix.objects.order.dto;

import lombok.Data;

@Data
public class UpdatePretixPositionRequest {
    private final String order;

    private final long item;
}
