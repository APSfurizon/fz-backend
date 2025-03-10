package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.NotNull;
import java.util.List;

@Data
public class UserAdminViewData {
    @NotNull
    private final UserAdminViewDisplay user;

    @NotNull
    private final List<Order> orders;
}
