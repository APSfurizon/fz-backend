package net.furizon.backend.feature.room.dto.request;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class ExchangeRoomRequest {
    @NotNull
    private final String sourceOrderCode;
    private final long sourceRoomPositionId;
    @Nullable
    private final Long sourceEarlyPositionId;
    @Nullable
    private final Long sourceLatePositionId;

    @NotNull
    private final String destOrderCode;
    private final long destRoomPositionId;
    @Nullable
    private final Long destEarlyPositionId;
    @Nullable
    private final Long destLatePositionId;

    @Nullable
    private final String manualPaymentComment;
    @Nullable
    private final String manualRefundComment;
}
