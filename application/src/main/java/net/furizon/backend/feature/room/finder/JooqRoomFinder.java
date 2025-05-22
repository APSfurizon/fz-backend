package net.furizon.backend.feature.room.finder;


import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.admin.dto.HotelExportRow;
import net.furizon.backend.feature.admin.dto.JooqRoomNotFullRow;
import net.furizon.backend.feature.admin.mapper.HotelExportRowMapper;
import net.furizon.backend.feature.admin.mapper.JooqRoomNotFullRowMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.room.mapper.JooqRoomDataMapper;
import net.furizon.backend.feature.room.mapper.JooqRoomInfoMapper;
import net.furizon.backend.feature.room.mapper.RoomGuestMapper;
import net.furizon.backend.feature.room.mapper.RoomGuestResponseMapper;
import net.furizon.backend.feature.room.mapper.RoomInvitationResponseMapper;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.jooq.generated.tables.Orders;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROOMS;
import static net.furizon.jooq.generated.Tables.ROOM_GUESTS;
import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqRoomFinder implements RoomFinder {
    @NotNull private final SqlQuery query;

    @Override
    public boolean isUserInAroom(long userId, long eventId, boolean ownRoomAllowed) {
        Condition c = ROOMS.ORDER_ID.eq(ORDERS.ID).and(ORDERS.EVENT_ID.eq(eventId));
        if (ownRoomAllowed) {
            c = c.and(ORDERS.USER_ID.notEqual(userId));
        }
        return query.fetchFirst(
            PostgresDSL.select(ROOM_GUESTS.ROOM_GUEST_ID)
            .from(ROOM_GUESTS)
            .innerJoin(ROOMS)
            .on(
                ROOM_GUESTS.USER_ID.eq(userId)
                .and(ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID))
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            ).innerJoin(ORDERS)
            .on(c)
            .limit(1)
        ).isPresent();
    }

    @Override
    public boolean isUserInvitedInRoom(long userId, long roomId) {
        return query.fetchFirst(
            PostgresDSL.select(ROOM_GUESTS.ROOM_GUEST_ID)
            .from(ROOM_GUESTS)
            .where(
                ROOM_GUESTS.USER_ID.eq(userId)
                .and(ROOM_GUESTS.ROOM_ID.eq(roomId))
                .and(ROOM_GUESTS.CONFIRMED.isFalse())
            )
            .limit(1)
        ).isPresent();
    }

    @Override
    public boolean userOwnsAroom(long userId, long eventId) {
        return query.fetchFirst(selectRoomsOwnedBy(userId, eventId).limit(1)).isPresent();
    }
    @Override
    public int countRoomsOwnedBy(long userId, long eventId) {
        return query.count(selectRoomsOwnedBy(userId, eventId));
    }
    private @NotNull SelectOnConditionStep<Record1<Long>> selectRoomsOwnedBy(long userId, long eventId) {
        return PostgresDSL.select(ROOMS.ROOM_ID)
                .from(ROOMS)
                .innerJoin(ORDERS)
                .on(
                        ROOMS.ORDER_ID.eq(ORDERS.ID)
                        .and(ORDERS.USER_ID.eq(userId))
                        .and(ORDERS.EVENT_ID.eq(eventId))
                );
    }
    @Override
    public int countRoomsWithUser(long userId, long eventId) {
        return query.count(
            PostgresDSL.selectDistinct(ROOM_GUESTS.ROOM_GUEST_ID)
            .from(ROOMS)
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(eventId))
            )
            .innerJoin(ROOM_GUESTS)
            .on(
                ROOMS.ROOM_ID.eq(ROOM_GUESTS.ROOM_ID)
                .and(ROOM_GUESTS.USER_ID.eq(userId))
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            )
        );
    }

    @Override
    public @Nullable String getRoomName(long roomId) {
        return query.fetchFirst(
                PostgresDSL.select(ROOMS.ROOM_NAME)
                .from(ROOMS)
                .where(ROOMS.ROOM_ID.eq(roomId))
        ).mapOrNull(r -> r.get(ROOMS.ROOM_NAME));
    }

    @Nullable
    @Override
    public RoomInfo getRoomInfoForUser(
            long userId, @NotNull Event event,
            @NotNull PretixInformation pretixInformation, @NotNull RoomLogic roomLogic
    ) {
        return query.fetchFirst(
            PostgresDSL.select(
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID,
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                ROOMS.ROOM_ID,
                ROOMS.ROOM_NAME,
                ROOMS.ROOM_CONFIRMED,
                ROOMS.SHOW_IN_NOSECOUNT,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                ORDERS.ORDER_ROOM_INTERNAL_NAME,
                ORDERS.ORDER_ROOM_CAPACITY
            )
            .from(ROOMS)
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
                .and(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID.isNotNull())
            )
            .innerJoin(ROOM_GUESTS)
            .on(
                ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID)
                .and(ROOM_GUESTS.USER_ID.eq(userId))
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            )
            .innerJoin(USERS)
            .on(USERS.USER_ID.eq(ORDERS.USER_ID))
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .limit(1)
        ).mapOrNull(k -> JooqRoomInfoMapper.map(k, pretixInformation, roomLogic, userId, event.getId()));
    }

    @Override
    public @Nullable RoomData getRoomDataFromPretixItemId(long pretixItemId,
                                                          @NotNull PretixInformation pretixInformation) {
        HotelCapacityPair pair = pretixInformation.getRoomInfoFromPretixItemId(pretixItemId);
        return new RoomData(
                pair == null ? 0 : pair.capacity(),
                pretixItemId,
                pair == null ? null : pair.roomInternalName(),
                pretixInformation.getRoomNamesFromRoomPretixItemId(pretixItemId)
        );
    }

    @Nullable
    @Override
    public RoomData getRoomDataForUser(
            long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation
    ) {
        return query.fetchFirst(
            PostgresDSL.select(
                ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                ORDERS.ORDER_ROOM_INTERNAL_NAME,
                ORDERS.ORDER_ROOM_CAPACITY
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
    public List<RoomGuest> getRoomGuestsFromRoomId(long roomId, boolean onlyConfirmed) {
        var condition = ROOM_GUESTS.ROOM_ID.eq(roomId);
        if (onlyConfirmed) {
            condition = condition.and(ROOM_GUESTS.CONFIRMED.isTrue());
        }
        return query.fetch(
                roomGuestSelect()
                .where(condition)
        ).stream().map(RoomGuestMapper::map).toList();
    }
    @NotNull
    @Override
    public List<RoomGuestResponse> getRoomGuestResponseFromRoomId(long roomId, @NotNull Event event) {
        return query.fetch(
            PostgresDSL.select(
                ROOM_GUESTS.ROOM_GUEST_ID,
                ROOM_GUESTS.USER_ID,
                ROOM_GUESTS.ROOM_ID,
                ROOM_GUESTS.CONFIRMED,
                ORDERS.ORDER_STATUS,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID
            )
            .from(ROOM_GUESTS)
            .innerJoin(USERS)
            .on(
                USERS.USER_ID.eq(ROOM_GUESTS.USER_ID)
                .and(ROOM_GUESTS.ROOM_ID.eq(roomId))
            )
            .leftJoin(ORDERS)
            .on(ORDERS.USER_ID.eq(USERS.USER_ID))
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .where(ORDERS.EVENT_ID.eq(event.getId()))
        ).stream().map(RoomGuestResponseMapper::map).toList();
    }

    @NotNull
    @Override
    public Optional<Long> getRoomIdFromOwnerUserId(long ownerUserId, @NotNull Event event) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL.select(ROOMS.ROOM_ID)
            .from(ROOMS)
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.USER_ID.eq(ownerUserId))
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .limit(1)
        ).mapOrNull(k -> k.get(ROOMS.ROOM_ID)));
    }

    @NotNull
    @Override
    public Optional<Long> getOwnerUserIdFromRoomId(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL.select(ORDERS.USER_ID)
            .from(ORDERS)
            .innerJoin(ROOMS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ROOMS.ROOM_ID.eq(roomId))
            )
            .limit(1)
        ).mapOrNull(k -> k.get(ORDERS.USER_ID)));
    }

    @Override
    public @NotNull Optional<Long> getEventIdOfRoom(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL.select(ORDERS.EVENT_ID)
            .from(ROOMS)
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ROOMS.ROOM_ID.eq(roomId))
            )
            .limit(1)
        ).mapOrNull(k -> k.get(ORDERS.EVENT_ID)));
    }

    @NotNull
    @Override
    public List<Long> getRoomsForEvent(long eventId) {
        return query.fetch(
            PostgresDSL.select(ROOMS.ROOM_ID)
            .from(ROOMS)
            .join(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(eventId))
            )
        ).stream().map(k -> k.get(ROOMS.ROOM_ID)).toList();
    }

    @NotNull
    @Override
    public List<RoomInvitationResponse> getUserReceivedInvitations(
            long userId, @NotNull Event event,
            @NotNull PretixInformation pretixService, @NotNull RoomLogic roomLogic) {
        return query.fetch(
            PostgresDSL.select(
                ROOM_GUESTS.ROOM_GUEST_ID,
                ROOM_GUESTS.USER_ID,
                ROOM_GUESTS.ROOM_ID,
                ROOM_GUESTS.CONFIRMED,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID,
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                ROOMS.ROOM_ID,
                ROOMS.ROOM_NAME,
                ROOMS.ROOM_CONFIRMED,
                ROOMS.SHOW_IN_NOSECOUNT,
                ORDERS.USER_ID,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                ORDERS.ORDER_ROOM_INTERNAL_NAME,
                ORDERS.ORDER_ROOM_CAPACITY
            )
            .from(ROOM_GUESTS)
            .innerJoin(ROOMS)
            .on(
                ROOM_GUESTS.USER_ID.eq(userId)
                .and(ROOM_GUESTS.CONFIRMED.isFalse())
                .and(ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID))
            )
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .innerJoin(USERS) //We want to fetch the room's owner data
            .on(USERS.USER_ID.eq(ORDERS.USER_ID))
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
        ).stream().map(k -> RoomInvitationResponseMapper.map(k, pretixService, roomLogic, event.getId())).toList();
    }

    @NotNull
    @Override
    public Optional<Boolean> isRoomConfirmed(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL.select(ROOMS.ROOM_CONFIRMED)
            .from(ROOMS)
            .where(ROOMS.ROOM_ID.eq((roomId)))
            .limit(1)
        ).mapOrNull(k -> k.get(ROOMS.ROOM_CONFIRMED)));
    }

    @NotNull
    @Override
    public Optional<RoomGuest> getConfirmedRoomGuestFromUserEvent(long userId, @NotNull Event event) {
        return Optional.ofNullable(query.fetchFirst(
            roomGuestSelect()
            .innerJoin(ROOMS)
            .on(
                ROOMS.ROOM_ID.eq(ROOM_GUESTS.ROOM_ID)
                .and(ROOM_GUESTS.USER_ID.eq(userId))
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            )
            .innerJoin(ORDERS)
            .on(
                ROOMS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .limit(1)
        ).mapOrNull(RoomGuestMapper::map));
    }

    @NotNull
    @Override
    public Optional<RoomGuest> getRoomGuestFromId(long roomGuestId) {
        return Optional.ofNullable(query.fetchFirst(
            roomGuestSelect()
            .where(ROOM_GUESTS.ROOM_GUEST_ID.eq((roomGuestId)))
            .limit(1)
        ).mapOrNull(RoomGuestMapper::map));
    }

    private @NotNull SelectJoinStep<Record4<Long, Long, Long, Boolean>> roomGuestSelect() {
        return PostgresDSL.select(
                ROOM_GUESTS.ROOM_GUEST_ID,
                ROOM_GUESTS.USER_ID,
                ROOM_GUESTS.ROOM_ID,
                ROOM_GUESTS.CONFIRMED
            )
            .from(ROOM_GUESTS);
    }

    @NotNull
    @Override
    public Optional<Short> getRoomCapacity(long roomId) {
        return Optional.ofNullable(query.fetchFirst(
            PostgresDSL.select(ORDERS.ORDER_ROOM_CAPACITY)
            .from(ROOMS)
            .innerJoin(ORDERS)
            .on(
                ROOMS.ROOM_ID.eq(roomId)
                .and(ORDERS.ID.eq(ROOMS.ORDER_ID))
            )
            .limit(1)
        ).mapOrNull(k -> k.get(ORDERS.ORDER_ROOM_CAPACITY)));
    }

    @Override
    public @Nullable Long getRoomItemIdFromRoomId(long roomId) {
        return query.fetchFirst(
            PostgresDSL.select(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID)
            .from(ORDERS)
            .innerJoin(ROOMS)
            .on(
                ROOMS.ROOM_ID.eq(roomId)
                .and(ORDERS.ID.eq(ROOMS.ORDER_ID))
            )
        ).mapOrNull(k -> k.get(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID));
    }

    @Override
    public @NotNull List<JooqRoomNotFullRow> getNotFullRooms(long eventId,
                                                             @NotNull PretixInformation pretixInformation) {
        /*
        SELECT
            t.owner_id,
            t.room_count,
            users.user_fursona_name,
            orders.order_room_pretix_item_id,
            orders.order_room_capacity
        FROM orders
        INNER JOIN (
            SELECT
                orders.user_id AS owner_id,
                COUNT(DISTINCT room_guests.user_id) AS room_count
            FROM orders
            LEFT JOIN rooms
            ON orders.id = rooms.order_id
            LEFT JOIN room_guests
            ON
                rooms.room_id = room_guests.room_id
                AND room_guests.confirmed IS TRUE
            WHERE
              orders.order_status = 2
              AND orders.order_room_pretix_item_id != 237
            GROUP BY orders.user_id
        ) t
        ON orders.user_id = t.owner_id
        INNER JOIN users
        ON orders.user_id = users.user_id
        WHERE t.room_count < orders.order_room_capacity;
         */
        Field<Integer> usersCount = PostgresDSL.field("users", Integer.class);

        Table<?> roomCounts = PostgresDSL.select(
                ORDERS.USER_ID,
                PostgresDSL.countDistinct(ROOM_GUESTS.USER_ID).as(usersCount)
            )
            .from(ORDERS)
            .leftJoin(ROOMS)
            .on(ORDERS.ID.eq(ROOMS.ORDER_ID))
            .leftJoin(ROOM_GUESTS)
            .on(
                ROOMS.ROOM_ID.eq(ROOM_GUESTS.ROOM_ID)
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            )
            .where(
                ORDERS.ORDER_STATUS.eq((short) OrderStatus.PAID.ordinal())
                .and(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID.isNotNull())
                .and(ORDERS.ORDER_ROOM_PRETIX_ITEM_ID.notIn(
                        pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM)
                ))
            )
            .groupBy(ORDERS.USER_ID)
            .asTable("roomCounts");

        return query.fetch(
            PostgresDSL.select(
                roomCounts.field(ORDERS.USER_ID),
                roomCounts.field(usersCount),
                ORDERS.ORDER_ROOM_PRETIX_ITEM_ID,
                ORDERS.ORDER_ROOM_CAPACITY
            )
            .from(ORDERS)
            .innerJoin(roomCounts)
            .on(ORDERS.USER_ID.eq(roomCounts.field(ORDERS.USER_ID)))
            .where(ORDERS.ORDER_ROOM_CAPACITY.greaterThan(roomCounts.field(usersCount).cast(Short.class)))
        ).stream().map(r -> JooqRoomNotFullRowMapper.map(r, usersCount)).toList();
    }

    @Override
    public @NotNull List<HotelExportRow> exportHotel(long eventId,
                                                     @NotNull RoomLogic roomLogic,
                                                     @NotNull PretixInformation pretixInformation) {
        Orders roomOwnerOrder = ORDERS.as("roomOwnerOrder");
        return query.fetch(
            PostgresDSL.select(
                roomOwnerOrder.ORDER_ROOM_PRETIX_ITEM_ID,
                ROOMS.ROOM_ID,
                MEMBERSHIP_INFO.INFO_FIRST_NAME,
                MEMBERSHIP_INFO.INFO_LAST_NAME,
                MEMBERSHIP_INFO.INFO_BIRTHDAY,
                MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY,
                MEMBERSHIP_INFO.INFO_BIRTH_REGION,
                MEMBERSHIP_INFO.INFO_BIRTH_CITY,
                MEMBERSHIP_INFO.INFO_COUNTRY,
                MEMBERSHIP_INFO.INFO_REGION,
                MEMBERSHIP_INFO.INFO_CITY,
                MEMBERSHIP_INFO.INFO_ZIP,
                MEMBERSHIP_INFO.INFO_ADDRESS,
                AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                MEMBERSHIP_INFO.INFO_PHONE_PREFIX,
                MEMBERSHIP_INFO.INFO_PHONE,
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                ORDERS.ORDER_CODE,
                roomOwnerOrder.ORDER_CODE,
                ORDERS.ORDER_REQUIRES_ATTENTION,
                ORDERS.ORDER_INTERNAL_COMMENT
            )
            .from(USERS)
            .innerJoin(MEMBERSHIP_INFO)
            .on(MEMBERSHIP_INFO.USER_ID.eq(USERS.USER_ID))
            .innerJoin(AUTHENTICATIONS)
            .on(AUTHENTICATIONS.USER_ID.eq(USERS.USER_ID))
            .innerJoin(ORDERS)
            .on(
                ORDERS.USER_ID.eq(USERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(eventId))
            )
            .innerJoin(ROOM_GUESTS)
            .on(
                USERS.USER_ID.eq(ROOM_GUESTS.USER_ID)
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            )
            .innerJoin(ROOMS)
            .on(ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID))
            .innerJoin(roomOwnerOrder)
            .on(
                ROOMS.ORDER_ID.eq(roomOwnerOrder.ID)
                .and(roomOwnerOrder.EVENT_ID.eq(eventId))
            )
            .orderBy(ROOMS.ROOM_ID, USERS.USER_ID)
        ).stream().map(e -> HotelExportRowMapper.map(
                e,
                roomOwnerOrder,
                eventId,
                roomLogic,
                pretixInformation
        )).toList();
    }

    @Override
    public @Nullable ExtraDays getExtraDaysOfRoomOwner(long guestUserId, long eventId) {
        return query.fetchFirst(
            PostgresDSL.select(ORDERS.ORDER_EXTRA_DAYS_TYPE)
            .from(ROOM_GUESTS)
            .innerJoin(ROOMS)
            .on(
                ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID)
                .and(ROOM_GUESTS.USER_ID.eq(guestUserId))
            )
            .innerJoin(ORDERS)
            .on(ROOMS.ORDER_ID.eq(ORDERS.ID))
        ).mapOrNull(r -> ExtraDays.get(r.get(ORDERS.ORDER_EXTRA_DAYS_TYPE)));
    }
}
