package net.furizon.backend.feature.pretix.ordersworkflow.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FullInfoResponse {
    private final boolean shouldDisplayCountdown;
    @Nullable
    private final LocalDateTime bookingStartTime;

    @Nullable
    private final LocalDateTime editBookEndTime;

    @Nullable
    private final Map<String, String> eventNames;

    private final boolean hasActiveMembershipForEvent;


    @Nullable
    private final OrderDataResponse order;

    @NotNull
    private final List<OrderWorkflowErrorCode> errors;
}
