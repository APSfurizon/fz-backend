package net.furizon.backend.feature.pretix.order.finder;

import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OrderFinder {
    @Nullable
    Order findOrderByCode(@NotNull String code);
}
