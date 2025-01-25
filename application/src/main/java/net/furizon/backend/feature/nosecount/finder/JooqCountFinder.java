package net.furizon.backend.feature.nosecount.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.mapper.JooqFursuitDisplayMapper;
import net.furizon.backend.feature.nosecount.dto.JooqNosecountObj;
import net.furizon.backend.feature.nosecount.mapper.JooqNosecountObjMapper;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.jooq.generated.tables.Orders;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.ROOMS;
import static net.furizon.jooq.generated.Tables.ROOM_GUESTS;
import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.Tables.FURSUITS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.FURSUITS_ORDERS;

@Component
@RequiredArgsConstructor
public class JooqCountFinder implements CountsFinder {
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public @NotNull List<FursuitDisplayData> getFursuits(long eventId) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                FURSUITS.FURSUIT_ID,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE
            )
            .from(FURSUITS)
            .innerJoin(FURSUITS_ORDERS)
            .on(
                FURSUITS.FURSUIT_ID.eq(FURSUITS_ORDERS.FURSUIT_ID)
                .and(FURSUITS.SHOW_IN_FURSUITCOUNT.isTrue())
            )
            .innerJoin(ORDERS)
            .on(
                FURSUITS_ORDERS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(eventId))
            )
            .leftJoin(MEDIA)
            .on(FURSUITS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .orderBy(FURSUITS.FURSUIT_NAME)
        ).stream().map(JooqFursuitDisplayMapper::mapWithOrder).toList();
    }

    @Override
    public @NotNull List<UserDisplayData> getSponsors(long eventId) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID,
                ORDERS.ORDER_SPONSORSHIP_TYPE
            )
            .from(USERS)
            .innerJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(USERS.SHOW_IN_NOSECOUNT.isTrue())
                .and(ORDERS.ORDER_SPONSORSHIP_TYPE.greaterThan((short) 0))
            )
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .orderBy(USERS.USER_FURSONA_NAME)
        ).stream().map(JooqUserDisplayMapper::map).toList();
    }


    @Override
    public @NotNull List<JooqNosecountObj> getNosecount(long eventId) {
        Orders roomOwnerOrder = ORDERS.as("roomOwnerOrder");
        return sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID,
                ORDERS.ORDER_SPONSORSHIP_TYPE,

                ORDERS.ORDER_DAILY_DAYS,

                ROOMS.ROOM_ID,
                ROOMS.ROOM_NAME,
                roomOwnerOrder.ORDER_ROOM_CAPACITY,
                roomOwnerOrder.ORDER_ROOM_PRETIX_ITEM_ID,
                roomOwnerOrder.ORDER_ROOM_INTERNAL_NAME,
                roomOwnerOrder.ORDER_HOTEL_INTERNAL_NAME
            )
            .from(USERS)
            .innerJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(eventId))
                .and(USERS.SHOW_IN_NOSECOUNT.isTrue())
            )

            .leftJoin(ROOM_GUESTS)
            .on(
                USERS.USER_ID.eq(ROOM_GUESTS.USER_ID)
                .and(ROOM_GUESTS.CONFIRMED.isTrue())
            )
            .leftJoin(ROOMS)
            .on(
                ROOM_GUESTS.ROOM_ID.eq(ROOMS.ROOM_ID)
                .and(ROOMS.SHOW_IN_NOSECOUNT.isTrue())
            )
            .leftJoin(roomOwnerOrder)
            .on(ROOMS.ORDER_ID.eq(roomOwnerOrder.ID))

            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .orderBy(ROOMS.ROOM_ID)
        ).stream().map(r -> JooqNosecountObjMapper.map(r, roomOwnerOrder)).toList();
    }
}
