package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class UserAdminViewData {
    @NotNull
    private final UserAdminViewDisplay user;

    @NotNull
    private final Map<Long, List<Order>> orders;
}
