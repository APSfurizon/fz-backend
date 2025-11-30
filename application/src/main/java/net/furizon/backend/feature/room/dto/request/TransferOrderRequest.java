package net.furizon.backend.feature.room.dto.request;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class TransferOrderRequest {
    @NotNull
    private final String orderCode;
    private final long positionId;
    private final long questionId;
    private final long newUserId;

    @Nullable
    private final String manualPaymentComment;
    @Nullable
    private final String manualRefundComment;
}
