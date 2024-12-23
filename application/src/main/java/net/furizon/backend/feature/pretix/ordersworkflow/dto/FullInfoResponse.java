package net.furizon.backend.feature.pretix.ordersworkflow.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FullInfoResponse {
    private final boolean shouldDisplayCountdown;
    @Nullable
    private final OffsetDateTime bookingStartTime;

    @Nullable
    private final OffsetDateTime editBookEndTime;

    @Nullable
    private final Map<String, String> eventNames;

    private final boolean hasActiveMembershipForEvent;

    private boolean shouldUpdateInfo;

    @Nullable
    private final OrderDataResponse order;

    @NotNull
    private final List<OrderWorkflowErrorCode> errors;
}
