package net.furizon.backend.feature.room.action.transferOrder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.TransferOrderRequest;
import org.jetbrains.annotations.NotNull;

public interface TransferPretixOrderAction {
    @Nullable String invoke(
            @NotNull TransferOrderRequest toReq,
            @NotNull Event event
    );
}
