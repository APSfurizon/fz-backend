package net.furizon.backend.feature.pretix.objects.order.action.changeOrder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.dto.request.ChangeOrderRequest;
import org.jetbrains.annotations.NotNull;

public interface ChangeOrderAction {
    boolean invoke(@NotNull ChangeOrderRequest changeOrderRequest,
                   @NotNull String orderCode,
                   @NotNull Event event);
}
