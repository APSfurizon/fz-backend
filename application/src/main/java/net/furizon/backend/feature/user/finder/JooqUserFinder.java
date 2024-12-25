package net.furizon.backend.feature.user.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.mapper.JooqDisplayUserMapper;
import net.furizon.backend.feature.user.mapper.JooqSearchUserMapper;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.backend.feature.user.objects.SearchUser;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.furizon.jooq.generated.Tables.*;

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

    @NotNull
    @Override
    public List<UserDisplayData> getDisplayUserByIds(Set<Long> ids, @NotNull Event event) {
        return sqlQuery.fetch(
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
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .leftJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .where(USERS.USER_ID.in(ids))
        ).stream().map(JooqDisplayUserMapper::map).toList();
    }

    @Nullable
    @Override
    public UserDisplayData getDisplayUser(long userId, @NotNull Event event) {
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
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .leftJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            ).where(USERS.USER_ID.eq(userId))
        ).mapOrNull(JooqDisplayUserMapper::map);
    }

    @NotNull
    @Override
    public List<SearchUser> searchUserInCurrentEvent(
            @NotNull String fursonaName,
            @NotNull Event event,
            boolean filterRoom,
            boolean filterPaid,
            @Nullable Short filterMembershipCardForYear
    ) {
        Condition condition = PostgresDSL.trueCondition();
        boolean joinOrders = false;
        boolean joinMembershipCards = false;

        if (filterRoom) {
            joinOrders = true;
            condition = condition.and(
                USERS.USER_ID.notIn(
                    PostgresDSL.select(ROOM_GUESTS.USER_ID)
                    .from(ROOM_GUESTS)
                    .where(ROOM_GUESTS.CONFIRMED.eq(true))
                )
                .and(
                    ORDERS.ORDER_ROOM_CAPACITY.isNull()
                    .or(ORDERS.ORDER_ROOM_CAPACITY.lessOrEqual((short) 0))
                )
            );
        }

        if (filterPaid) {
            joinOrders = true;
            condition = condition.and(
                ORDERS.ORDER_STATUS.eq((short) OrderStatus.PAID.ordinal())
            );
        }

        if (filterMembershipCardForYear != null) {
            condition = condition.and( //TODO test, it might be broken
                MEMBERSHIP_CARDS.ISSUE_YEAR.notEqual(filterMembershipCardForYear)
            );
        }

        SelectJoinStep<?> query = PostgresDSL
            .selectDistinct(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                MEDIA.MEDIA_PATH
            )
            .from(
                PostgresDSL.select(
                    USERS.USER_LOCALE,
                    USERS.USER_ID,
                    USERS.USER_FURSONA_NAME,
                    USERS.MEDIA_ID_PROPIC,
                    USERS.MEDIA_ID_PROPIC
                )
                .from(USERS)
                .where(
                    USERS.USER_FURSONA_NAME.likeIgnoreCase("%" + fursonaName + "%")
                    .and(USERS.SHOW_IN_NOSECOUNT.eq(true))
                    .or(
                        //If someone doesn't want to be displayed in the nosecount,
                        // find him only if it's a almost exact match
                        USERS.USER_FURSONA_NAME.like("_" + fursonaName + "_")
                        .and(USERS.SHOW_IN_NOSECOUNT.eq(false))
                    )
                )
            )
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID));

        if (joinOrders) {
            query = query
                    .leftJoin(ORDERS)
                    .on(USERS.USER_ID.eq(ORDERS.USER_ID));
        }

        if (joinMembershipCards) {
            query = query
                    .leftJoin(MEMBERSHIP_CARDS)
                    .on(USERS.USER_ID.eq(MEMBERSHIP_CARDS.USER_ID));
        }

        return sqlQuery.fetch(
            query
            .where(condition)
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
