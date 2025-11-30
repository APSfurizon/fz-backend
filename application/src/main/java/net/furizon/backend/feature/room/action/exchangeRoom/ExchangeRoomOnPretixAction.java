package net.furizon.backend.feature.room.action.exchangeRoom;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExchangeRoomOnPretixAction {
    boolean invoke(
        @NotNull String sourceOrderCode,
        long sourceRoomPositionId,
        @Nullable Long sourceEarlyPositionId,
        @Nullable Long sourceLatePositionId,

        @NotNull String destOrderCode,
        long destRoomPositionId,
        @Nullable Long destEarlyPositionId,
        @Nullable Long destLatePositionId,

        @Nullable String manualPaymentComment,
        @Nullable String manualRefundComment,
        @NotNull Event event
    );
}
