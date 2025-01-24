package net.furizon.backend.feature.fursuits.action.bringFursuitToEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.FURSUITS_ORDERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpdateBringFursuitToEventAction implements UpdateBringFursuitToEventAction {
    @NotNull private final SqlCommand command;

    @Override
    public boolean invoke(long fursuitId, boolean bringing, @NotNull Order linkedOrder) {
        //If order does not exist, due to ON CASCADE clause, this is automatically deleted as well
        log.info("Setting fursuit {} bringToEvent = {}", fursuitId, bringing);
        if (!bringing) {
            return command.execute(
                PostgresDSL.deleteFrom(FURSUITS_ORDERS)
                .where(
                    FURSUITS_ORDERS.FURSUIT_ID.eq(fursuitId)
                    .and(FURSUITS_ORDERS.ORDER_ID.eq(linkedOrder.getId()))
                )
            ) > 0;
        } else {
            command.execute(
                PostgresDSL.insertInto(
                    FURSUITS_ORDERS,
                    FURSUITS_ORDERS.FURSUIT_ID,
                    FURSUITS_ORDERS.ORDER_ID
                )
                .values(
                    fursuitId,
                    linkedOrder.getId()
                )
            );
            return true;
        }
    }
}
