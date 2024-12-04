package net.furizon.backend.feature.user.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.dto.UserDisplayDataResponse;
import net.furizon.backend.feature.user.mapper.JooqDisplayUserMapper;
import net.furizon.backend.feature.user.mapper.JooqSearchUserMapper;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROOM_GUESTS;
import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqUserFinder implements UserFinder {
    private final SqlQuery sqlQuery;

    @Nullable
    @Override
    public User findById(long userId) {
        return sqlQuery
            .fetchFirst(
                selectUser()
                    .where(USERS.USER_ID.eq(userId))
            )
            .mapOrNull(JooqUserMapper::map);
    }

    @Nullable
    @Override
    public UserDisplayDataResponse getDisplayUser(long userId) {
        return sqlQuery.fetchFirst(
            PostgresDSL
            .select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                ORDERS.ORDER_SPONSORSHIP_TYPE
            )
            .from(USERS)
            .leftJoin(MEDIA)
            .on(
                USERS.USER_ID.eq(userId)
                .and(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            )
            .leftJoin(ORDERS)
            .on(USERS.USER_ID.eq(ORDERS.USER_ID))
        ).mapOrNull(JooqDisplayUserMapper::map);
    }

    @NotNull
    @Override
    public List<SearchUsersResponse.SearchUser> searchUserInCurrentEvent(
            @NotNull String fursonaName,
            @NotNull Event event,
            boolean filterRoom
    ) {
        var query = PostgresDSL
            .select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                MEDIA.MEDIA_PATH
            )
            .from(USERS)
            .innerJoin(MEDIA)
            .on(
                USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID)
                .and(
                    USERS.USER_FURSONA_NAME.like("%" + fursonaName + "%")
                    .and(USERS.SHOW_IN_NOSECOUNT.eq(true))
                    .or(
                        //If someone doesn't want to be displayed in the nosecount,
                        // find him only if it's a almost exact match
                        USERS.USER_FURSONA_NAME.like("_" + fursonaName + "_")
                        .and(USERS.SHOW_IN_NOSECOUNT.eq(false))
                    )
                )
            );

        if (filterRoom) {
            query = query
                .innerJoin(ORDERS)
                .on(
                    USERS.USER_ID.eq(ORDERS.USER_ID)
                    .and(
                        ORDERS.ORDER_ROOM_CAPACITY.isNull()
                        .or(ORDERS.ORDER_ROOM_CAPACITY.lessOrEqual((short) 0))
                    )
                    .and(
                        USERS.USER_ID.notIn(
                            PostgresDSL.select(ROOM_GUESTS.USER_ID)
                            .from(ROOM_GUESTS)
                            .where(ROOM_GUESTS.CONFIRMED.eq(true))
                        )
                    )
                );
        }

        return sqlQuery.fetch(
            query
            .orderBy(
                PostgresDSL.position(fursonaName, USERS.USER_FURSONA_NAME),
                USERS.USER_FURSONA_NAME
            )
        ).stream().map(JooqSearchUserMapper::map).toList();
    }

    private SelectJoinStep<?> selectUser() {
        return PostgresDSL
            .select(
                    USERS.USER_ID,
                    USERS.USER_FURSONA_NAME,
                    USERS.USER_LOCALE,
                    USERS.MEDIA_ID_PROPIC,
                    USERS.SHOW_IN_NOSECOUNT
            )
            .from(USERS);
    }
}
