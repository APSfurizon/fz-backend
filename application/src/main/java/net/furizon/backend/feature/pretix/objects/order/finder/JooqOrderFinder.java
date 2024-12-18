package net.furizon.backend.feature.pretix.objects.order.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.mapper.JooqOrderMapper;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqOrderFinder implements OrderFinder {
    private final SqlQuery query;

    private final JooqOrderMapper orderMapper;

    @Override
    public @Nullable Order findOrderByCodeEvent(@NotNull String code,
                                                @NotNull Event event,
                                                @NotNull PretixInformation pretixService) {
        return query.fetchFirst(
            selectFrom()
            .where(
                ORDERS.ORDER_CODE.eq(code))
                .and(ORDERS.EVENT_ID.eq(event.getId())
            )
            .limit(1)
        ).mapOrNull(e -> orderMapper.map(e, pretixService));
    }

    @Override
    public @Nullable Order findOrderByUserIdEvent(long userId,
                                                  @NotNull Event event,
                                                  @NotNull PretixInformation pretixService) {
        return query.fetchFirst(
            selectFrom()
            .where(
                ORDERS.USER_ID.eq(userId))
                .and(ORDERS.EVENT_ID.eq(event.getId())
            )
            .limit(1)
        ).mapOrNull(e -> orderMapper.map(e, pretixService));
    }

    @Override
    public int countOrdersOfUserOnEvent(long userId, @NotNull Event event) {
        return query.count(
            PostgresDSL.select(ORDERS.ID)
            .from(ORDERS)
            .where(
                ORDERS.USER_ID.eq(userId))
                .and(ORDERS.EVENT_ID.eq(event.getId())
            )
        );
    }

    @Override
    public @NotNull Optional<Boolean> isOrderDaily(long userId, @NotNull Event event) {
        Boolean res = query.fetchFirst(
                PostgresDSL.select(ORDERS.ORDER_DAILY_DAYS)
                .from(ORDERS)
                .where(
                    ORDERS.USER_ID.eq(userId))
                    .and(ORDERS.EVENT_ID.eq(event.getId())
                )
                .limit(1)
        ).mapOrNull(e -> e.get(ORDERS.ORDER_DAILY_DAYS) != 0L);
        return Optional.ofNullable(res);
    }

    @Override
    public @NotNull Optional<OrderStatus> getOrderStatus(long userId, @NotNull Event event) {
        return Optional.ofNullable(
            query.fetchFirst(
                PostgresDSL.select(ORDERS.ORDER_STATUS)
                .from(ORDERS)
                .where(
                    ORDERS.USER_ID.eq(userId))
                    .and(ORDERS.EVENT_ID.eq(event.getId())
                )
                .limit(1)
            ).mapOrNull(e -> OrderStatus.values()[e.get(ORDERS.ORDER_STATUS)])
        );
    }

    @Override
    public @NotNull Optional<Boolean> userHasBoughtAroom(long userId, @NotNull Event event) {
        return Optional.ofNullable(
            query.fetchFirst(
                PostgresDSL.select(ORDERS.ORDER_ROOM_CAPACITY)
                .from(ORDERS)
                .where(
                    ORDERS.USER_ID.eq(userId))
                    .and(ORDERS.EVENT_ID.eq(event.getId())
                )
                .limit(1)
            ).mapOrNull(e -> {
                Short capacity = e.get(ORDERS.ORDER_ROOM_CAPACITY);
                return capacity == null ? false : capacity > 0;
            })
        );
    }

    private @NotNull SelectJoinStep<?> selectFrom() {
        return PostgresDSL.select(
                        ORDERS.ORDER_CODE,
                        ORDERS.ORDER_STATUS,
                        ORDERS.ORDER_SPONSORSHIP_TYPE,
                        ORDERS.ORDER_EXTRA_DAYS_TYPE,
                        ORDERS.ORDER_DAILY_DAYS,
                        ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                        ORDERS.ORDER_ROOM_CAPACITY,
                        ORDERS.ORDER_HOTEL_INTERNAL_NAME,
                        ORDERS.ORDER_SECRET,
                        ORDERS.HAS_MEMBERSHIP,
                        ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID,
                        ORDERS.ORDER_ANSWERS_JSON,
                        ORDERS.USER_ID,
                        ORDERS.EVENT_ID
                ).from(ORDERS);
    }
}
