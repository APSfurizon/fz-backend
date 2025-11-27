package net.furizon.backend.feature.pretix.objects.order.dto.request;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class ConvertTicketRequest {
    @NotNull
    private final String orderCode;
    private final long rootPositionId;
    private final long newRootItemId;
    @Nullable
    private final Long newRootItemVariationId;
}
