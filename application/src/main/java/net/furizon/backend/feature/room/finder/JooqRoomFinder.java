package net.furizon.backend.feature.room.finder;


import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomDataResponse;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.mapper.JooqRoomDataMapper;
import net.furizon.backend.feature.room.mapper.JooqRoomInfoMapper;
import net.furizon.backend.feature.room.mapper.RoomGuestResponseMapper;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record4;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static net.furizon.jooq.generated.tables.Rooms.ROOMS;
import static net.furizon.jooq.generated.tables.Orders.ORDERS;
import static net.furizon.jooq.generated.tables.RoomGuests.ROOM_GUESTS;

@Component
@RequiredArgsConstructor
public class JooqRoomFinder implements RoomFinder {
    @NotNull private final SqlQuery query;

    @Override
    public boolean isUserInAroom(long userId, long eventId) {
        return query.fetchFirst(
                PostgresDSL.select(ROOM_GUESTS.ROOM_GUEST_ID)
                .from(ROOM_GUESTS)
                .innerJoin(ROOMS)
                .on(
                    ROOM_GUESTS.USER_ID.eq(userId)
                    .and(ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID))
                    .and(ROOM_GUESTS.CONFIRMED.eq(true))
                ).innerJoin(ORDERS)
                .on(
                    ROOMS.ORDER_ID.eq(ORDERS.ID)
                    .and(ORDERS.EVENT_ID.eq(eventId))
                )
                .limit(1)
        ).isPresent();
    }

    @Override
    public boolean userOwnsAroom(long userId, long eventId) {
        return query.fetchFirst(
                PostgresDSL.select(ROOMS.ROOM_ID)
                .from(ROOMS)
                .innerJoin(ORDERS)
                .on(
                    ROOMS.ORDER_ID.eq(ORDERS.ID)
                    .and(ORDERS.USER_ID.eq(userId))
                    .and(ORDERS.EVENT_ID.eq(eventId))
                )
                .limit(1)
        ).isPresent();
    }

    @Nullable
    @Override
    public RoomInfo getRoomInfoForUser(
            long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation
    ) {
        return query.fetchFirst(
            PostgresDSL
            .select(
                ROOMS.ROOM_ID,
                ROOMS.ROOM_NAME,
                ORDERS.USER_ID,
                ROOMS.ROOM_CONFIRMED,
                ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                ORDERS.ORDER_ROOM_CAPACITY,
                ORDERS.ORDER_HOTEL_INTERNAL_NAME
            )
            .from(ROOMS)
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .limit(1)
        ).mapOrNull(k -> JooqRoomInfoMapper.map(k, pretixInformation));
    }

    @Nullable
    @Override
    public RoomDataResponse getRoomDataForUser(
            long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation
    ) {
        return query.fetchFirst(
            PostgresDSL.select(
                    ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                    ORDERS.ORDER_ROOM_CAPACITY,
                    ORDERS.ORDER_HOTEL_INTERNAL_NAME
            )
            .from(ORDERS)
            .where(
                ORDERS.USER_ID.eq(userId)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .limit(1)
        ).mapOrNull(k -> JooqRoomDataMapper.map(k, pretixInformation));
    }

    @NotNull
    @Override
    public List<RoomGuestResponse> getRoomGuestsFromRoomId(long roomId, boolean onlyConfirmed) {
        var condition = ROOM_GUESTS.ROOM_ID.eq(roomId);
        if (onlyConfirmed) {
            condition = condition.and(ROOM_GUESTS.CONFIRMED.eq(true));
        }
        return query.fetch(
                roomGuestSelect()
                .where(condition)
        ).stream().map(RoomGuestResponseMapper::map).toList();
    }

    @NotNull
    @Override
    public Optional<Long> getRoomIdFromOwnerUserId(long userId, @NotNull Event event) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL
                .select(ROOMS.ROOM_ID)
                .from(ROOMS)
                .innerJoin(ORDERS)
                .on(
                    ROOMS.ORDER_ID.eq(ORDERS.ID)
                    .and(ORDERS.USER_ID.eq(userId))
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                )
                    .limit(1)
        ).mapOrNull(k -> k.get(ROOMS.ROOM_ID)));
    }

    @NotNull
    public Optional<Long> getOwnerUserIdFromRoomId(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL
                .select(ORDERS.USER_ID)
                .from(ORDERS)
                .innerJoin(ROOMS)
                .on(
                    ROOMS.ORDER_ID.eq(ORDERS.ID)
                    .and(ROOMS.ROOM_ID.eq(roomId))
                )
                .limit(1)
        ).mapOrNull(k -> k.get(ORDERS.USER_ID)));
    }

    @NotNull
    @Override
    public List<RoomGuestResponse> getUserReceivedInvitations(long userId, @NotNull Event event) {
        return query.fetch(
                roomGuestSelect()
                .innerJoin(ROOMS)
                .on(
                    ROOM_GUESTS.USER_ID.eq(userId)
                    .and(ROOM_GUESTS.CONFIRMED.eq(false))
                    .and(ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID))
                )
                .innerJoin(ORDERS)
                .on(
                    ROOMS.ORDER_ID.eq(ORDERS.ID)
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                )
        ).stream().map(RoomGuestResponseMapper::map).toList();
    }

    @NotNull
    @Override
    public Optional<Boolean> isRoomConfirmed(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL
           .select(ROOMS.ROOM_CONFIRMED)
            .from(ROOMS)
            .where(ROOMS.ROOM_ID.eq((roomId)))
            .limit(1)
        ).mapOrNull(k -> k.get(ROOMS.ROOM_CONFIRMED)));
    }

    @NotNull
    @Override
    public Optional<RoomGuestResponse> getRoomGuestFromId(long roomGuestId) {
        return Optional.ofNullable(query.fetchFirst(
                roomGuestSelect()
                .where(ROOM_GUESTS.ROOM_GUEST_ID.eq((roomGuestId)))
                .limit(1)
        ).mapOrNull(RoomGuestResponseMapper::map));
    }

    private @NotNull SelectJoinStep<Record4<Long, Long, Long, Boolean>> roomGuestSelect() {
        return PostgresDSL
                .select(
                        ROOM_GUESTS.ROOM_GUEST_ID,
                        ROOM_GUESTS.USER_ID,
                        ROOM_GUESTS.ROOM_ID,
                        ROOM_GUESTS.CONFIRMED
                ).from(ROOM_GUESTS);
    }

    @NotNull
    @Override
    public Optional<Short> getRoomCapacity(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
                PostgresDSL.select(
                        ORDERS.ORDER_ROOM_CAPACITY
                ).from(ORDERS)
                .innerJoin(ROOMS)
                .on(
                        ROOMS.ROOM_ID.eq(roomId)
                        .and(ORDERS.ID.eq(ROOMS.ORDER_ID))
                ).limit(1)
        ).mapOrNull(k -> k.get(ORDERS.ORDER_ROOM_CAPACITY)));
    }
}
