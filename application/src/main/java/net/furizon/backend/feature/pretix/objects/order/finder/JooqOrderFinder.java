package net.furizon.backend.feature.pretix.objects.order.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.mapper.JooqOrderMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.JSON;
import org.jooq.Record13;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqOrderFinder implements OrderFinder {
    private final SqlQuery query;

    private final JooqOrderMapper orderMapper;

    @Override
    public @Nullable Order findOrderByCodeEvent(@NotNull String code, @NotNull Event event) {
        return query.fetchFirst(
            PostgresDSL.select(
                    ORDERS.ORDER_CODE,
                    ORDERS.ORDER_STATUS,
                    ORDERS.ORDER_SPONSORSHIP_TYPE,
                    ORDERS.ORDER_EXTRA_DAYS_TYPE,
                    ORDERS.ORDER_DAILY_DAYS,
                    ORDERS.ORDER_ROOM_CAPACITY,
                    ORDERS.ORDER_HOTEL_LOCATION,
                    ORDERS.ORDER_SECRET,
                    ORDERS.HAS_MEMBERSHIP,
                    ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID,
                    ORDERS.ORDER_ANSWERS_JSON,
                    ORDERS.USER_ID,
                    ORDERS.EVENT_ID
                )
            .from(ORDERS)
            .where(ORDERS.ORDER_CODE.eq(code))
            .and(ORDERS.EVENT_ID.eq(event.getId()))
        ).mapOrNull(orderMapper::map);
    }

    @Override
    public int countOrdersOfUserOnEvent(long userId, @NotNull Event event) {
        return query.count(
            PostgresDSL.select(ORDERS.ID)
            .where(ORDERS.USER_ID.eq(userId))
            .and(ORDERS.EVENT_ID.eq(event.getId()))
        );
    }
}
