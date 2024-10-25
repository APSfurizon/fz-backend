package net.furizon.backend.feature.pretix.order.action.delete;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqDeleteOrderAction implements DeleteOrderAction {
    private final SqlCommand command;

    @Override
    public void invoke(@NotNull Order order) {
        this.invoke(order.getCode());
    }

    @Override
    public void invoke(@NotNull String code) {
        command.execute(
            PostgresDSL
                .deleteFrom(ORDERS)
                .where(ORDERS.ORDER_CODE.eq(code))
        );
    }
}
