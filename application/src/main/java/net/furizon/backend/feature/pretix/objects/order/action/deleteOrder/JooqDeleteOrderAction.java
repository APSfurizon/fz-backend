package net.furizon.backend.feature.pretix.objects.order.action.deleteOrder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqDeleteOrderAction implements DeleteOrderAction {
    private final SqlCommand command;

    @Override
    public void invoke(@NotNull final Order order) {
        this.invoke(order.getCode());
    }

    @Override
    public void invoke(@NotNull final String code) {
        command.execute(
            PostgresDSL.deleteFrom(ORDERS)
            .where(ORDERS.ORDER_CODE.eq(code))
        );
    }

    @Override
    public void invoke(@NotNull Set<String> orderCodes) {
        command.execute(
            PostgresDSL.deleteFrom(ORDERS)
            .where(ORDERS.ORDER_CODE.in(orderCodes))
        );
    }
}
