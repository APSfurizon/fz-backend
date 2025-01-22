package net.furizon.backend.feature.pretix.objects.order.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.mapper.JooqOrderMapper;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.OrderDataResponse;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqOrderFinder implements OrderFinder {
    @NotNull private final SqlQuery query;

    @NotNull private final FursuitConfig fursuitConfig;

    @NotNull private final JooqOrderMapper orderMapper;

    @Override
    public @NotNull Set<String> findOrderCodesForEvent(@NotNull Event event) {
        return query.fetch(
            PostgresDSL.select(ORDERS.ORDER_CODE)
            .from(ORDERS)
            .where(ORDERS.EVENT_ID.eq(event.getId()))
        ).stream().map(s -> s.get(ORDERS.ORDER_CODE)).collect(Collectors.toSet());
    }
    @Override
    public @NotNull Set<Long> findOrderIdsForEvent(@NotNull Event event) {
        return query.fetch(
            PostgresDSL.select(ORDERS.ID)
            .from(ORDERS)
            .where(ORDERS.EVENT_ID.eq(event.getId()))
        ).stream().map(s -> s.get(ORDERS.ID)).collect(Collectors.toSet());
    }

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
        return Optional.ofNullable(query.fetchFirst(
                PostgresDSL.select(ORDERS.ORDER_DAILY_DAYS)
                .from(ORDERS)
                .where(
                    ORDERS.USER_ID.eq(userId))
                    .and(ORDERS.EVENT_ID.eq(event.getId())
                )
                .limit(1)
        ).mapOrNull(e -> e.get(ORDERS.ORDER_DAILY_DAYS) != 0L));
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

    @Override
    public @Nullable OrderDataResponse getOrderDataResponseFromUserEvent(long userId, @NotNull Event event,
                                                                         @NotNull PretixInformation pretixService) {
        OrderDataResponse orderDataResponse = null;
        Order order = findOrderByUserIdEvent(userId, event, pretixService);
        if (order != null) {
            boolean isDaily = order.isDaily();
            var orderDataBuilder = OrderDataResponse.builder()
                    .code(order.getCode())
                    .orderStatus(order.getOrderStatus())
                    .sponsorship(order.getSponsorship())
                    .extraDays(order.getExtraDays())
                    .isDailyTicket(isDaily);

            OffsetDateTime from = event.getDateFrom();
            if (isDaily && from != null) {
                orderDataBuilder = orderDataBuilder.dailyDays(
                        order.getDailyDays().stream().map(
                                d -> from.plusDays(d).toLocalDate()
                        ).collect(Collectors.toSet())
                );
            }
            if (order.hasRoom()) {
                short roomCapacity = order.getRoomCapacity();
                long roomItemId = Objects.requireNonNull(order.getPretixRoomItemId());
                orderDataBuilder = orderDataBuilder.room(
                        new RoomData(
                                roomCapacity,
                                roomItemId,
                                pretixService.getRoomNamesFromRoomPretixItemId(roomItemId)
                        )
                );
            }

            orderDataBuilder.totalFursuits((short) (fursuitConfig.getDefaultFursuitsNo() + order.getExtraFursuits()));

            orderDataResponse = orderDataBuilder.build();
        }
        return orderDataResponse;
    }

    @Override
    public @Nullable Short getBoughtExtraFursuits(long userId, @NotNull Event event) {
        return query.fetchFirst(
            PostgresDSL.select(ORDERS.ORDER_EXTRA_FURSUITS)
            .from(ORDERS)
            .where(
                ORDERS.USER_ID.eq(userId))
                .and(ORDERS.EVENT_ID.eq(event.getId())
            )
        ).mapOrNull(r -> r.get(ORDERS.ORDER_EXTRA_FURSUITS));
    }

    @Override
    public @Nullable String getOrderCodeById(long orderId) {
        return query.fetchFirst(
            PostgresDSL.select(ORDERS.ORDER_CODE)
            .from(ORDERS)
            .where(ORDERS.ID.eq(orderId))
        ).mapOrNull(r -> r.get(ORDERS.ORDER_CODE));
    }
    @Override
    public @Nullable Long getOrderIdByCode(@NotNull String orderCode) {
        return query.fetchFirst(
            PostgresDSL.select(ORDERS.ID)
            .from(ORDERS)
            .where(ORDERS.ORDER_CODE.eq(orderCode))
        ).mapOrNull(r -> r.get(ORDERS.ID));
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
                        ORDERS.ORDER_ROOM_INTERNAL_NAME,
                        ORDERS.ORDER_SECRET,
                        ORDERS.HAS_MEMBERSHIP,
                        ORDERS.ORDER_TICKET_POSITION_ID,
                        ORDERS.ORDER_TICKET_POSITION_POSITIONID,
                        ORDERS.ORDER_ROOM_POSITION_ID,
                        ORDERS.ORDER_ROOM_POSITION_POSITIONID,
                        ORDERS.ORDER_EARLY_POSITION_ID,
                        ORDERS.ORDER_LATE_POSITION_ID,
                        ORDERS.ORDER_ANSWERS_JSON,
                        ORDERS.ORDER_EXTRA_FURSUITS,
                        ORDERS.USER_ID,
                        ORDERS.EVENT_ID
                ).from(ORDERS);
    }
}
