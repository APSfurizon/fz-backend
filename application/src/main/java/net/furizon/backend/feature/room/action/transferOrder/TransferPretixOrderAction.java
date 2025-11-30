package net.furizon.backend.feature.room.action.transferOrder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TransferPretixOrderAction {
    boolean invoke(
        @NotNull String orderCode,
        long positionId,
        long questionId,
        long newUserId,

        @Nullable String paymentComment,
        @Nullable String refundComment,

        @NotNull Event event
    );
}
