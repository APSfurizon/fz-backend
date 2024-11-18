package net.furizon.backend.feature.pretix.objects.order.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OrderFinder {
    @Nullable
    Order findOrderByCodeEvent(@NotNull String code, @NotNull Event event, @NotNull PretixInformation pretixService);

    @Nullable
    Order findOrderByUserIdEvent(long userId, @NotNull Event event, @NotNull PretixInformation pretixService);

    int countOrdersOfUserOnEvent(long userId, @NotNull Event event);
}
