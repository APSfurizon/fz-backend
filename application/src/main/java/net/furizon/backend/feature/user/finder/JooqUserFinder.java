package net.furizon.backend.feature.user.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.backend.feature.user.mapper.JooqSearchUserMapper;
import net.furizon.backend.feature.user.mapper.JooqUserEmailDataMapper;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.backend.feature.user.objects.SearchUserResult;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.ROLES;
import static net.furizon.jooq.generated.Tables.ROOM_GUESTS;
import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqUserFinder implements UserFinder {
    @NotNull private final SqlQuery sqlQuery;

    @Nullable
    @Override
    public User findById(long userId) {
        return sqlQuery.fetchFirst(
                selectUser()
                .where(USERS.USER_ID.eq(userId))
            )
            .mapOrNull(JooqUserMapper::map);
    }

    @NotNull
    @Override
    public List<UserDisplayData> getDisplayUserByIds(Set<Long> ids, @NotNull Event event) {
        return sqlQuery.fetch(
            selectJoinDisplayUser(event.getId())
            .where(USERS.USER_ID.in(ids))
        ).stream().map(JooqUserDisplayMapper::map).toList();
    }

    @Nullable
    @Override
    public UserDisplayData getDisplayUser(long userId, @NotNull Event event) {
        return sqlQuery.fetchFirst(
            selectJoinDisplayUser(event.getId())
            .where(USERS.USER_ID.eq(userId))
        ).mapOrNull(JooqUserDisplayMapper::map);
    }

    @Nullable
    @Override
    public UserEmailData getMailDataForUser(long userId) {
        return sqlQuery.fetchFirst(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                AUTHENTICATIONS.AUTHENTICATION_EMAIL
            )
            .from(USERS)
            .innerJoin(AUTHENTICATIONS)
            .on(
                USERS.USER_ID.eq(AUTHENTICATIONS.USER_ID)
                .and(USERS.USER_ID.eq(userId))
            )
        ).mapOrNull(JooqUserEmailDataMapper::map);
    }
    @NotNull
    @Override
    public List<UserEmailData> getMailDataForUsers(@NotNull List<Long> userIds) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                AUTHENTICATIONS.AUTHENTICATION_EMAIL
            )
            .from(USERS)
            .innerJoin(AUTHENTICATIONS)
            .on(
                USERS.USER_ID.eq(AUTHENTICATIONS.USER_ID)
                .and(USERS.USER_ID.in(userIds))
            )
        ).stream().map(JooqUserEmailDataMapper::map).toList();
    }
    @NotNull
    @Override
    public List<UserEmailData> getMailDataForUsersWithNoPropic(@NotNull Event event) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                AUTHENTICATIONS.AUTHENTICATION_EMAIL
            )
            .from(USERS)
            .innerJoin(AUTHENTICATIONS)
            .on(
                USERS.USER_ID.eq(AUTHENTICATIONS.USER_ID)
                .and(USERS.MEDIA_ID_PROPIC.isNull())
            )
            .innerJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.ORDER_STATUS.eq((short) OrderStatus.PAID.ordinal()))
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
        ).stream().map(JooqUserEmailDataMapper::map).toList();
    }

    @NotNull
    @Override
    public List<SearchUserResult> searchUserInCurrentEvent(
            @NotNull String inputQuery,
            boolean isAdminSearch,
            @NotNull Event event,
            boolean filterRoom,
            boolean filterPaid,
            boolean filerNotMadeAnOrder,
            @Nullable Short filterMembershipCardForYear,
            @Nullable Boolean filterBanStatus,
            @Nullable String filterWithoutRole
    ) {
        Condition condition = PostgresDSL.trueCondition();
        boolean leftJoinOrders = false;
        boolean joinMembershipCards = false;
        boolean joinBanStatus = false;
        boolean joinOrders = false;

        var searchFursonaQuerySelect = selectUser();
        Condition searchFursonaQueryCondition;
        if (isAdminSearch) {
            String[] sp = inputQuery.split("\\s+");
            String s = String.join("%", sp);
            searchFursonaQueryCondition = PostgresDSL.concat(
                PostgresDSL.concat(MEMBERSHIP_INFO.INFO_FIRST_NAME, " "),
                PostgresDSL.concat(MEMBERSHIP_INFO.INFO_LAST_NAME, " "),
                MEMBERSHIP_INFO.INFO_FIRST_NAME //First name repeated two times so both A B and B A return results
            ).likeIgnoreCase("%" + s + "%");
            //for (String ss : sp) {
            searchFursonaQueryCondition = searchFursonaQueryCondition
                                          .or(USERS.USER_FURSONA_NAME.likeIgnoreCase("%" + inputQuery + "%"));
            //}
            searchFursonaQuerySelect = searchFursonaQuerySelect
                    .innerJoin(MEMBERSHIP_INFO)
                    .on(USERS.USER_ID.eq(MEMBERSHIP_INFO.USER_ID));
        } else {
            searchFursonaQueryCondition =
                USERS.USER_FURSONA_NAME.likeIgnoreCase("%" + inputQuery + "%")
                .and(USERS.SHOW_IN_NOSECOUNT.isTrue())
                .or(
                    //If someone doesn't want to be displayed in the nosecount,
                    // find him only if it's a almost exact match
                    USERS.USER_FURSONA_NAME.like("_" + inputQuery + "_")
                    .and(USERS.SHOW_IN_NOSECOUNT.isFalse())
                );
        }

        Table<?> searchFursonaQuery = searchFursonaQuerySelect.where(searchFursonaQueryCondition).asTable("fursonaq");

        if (filterRoom) {
            joinOrders = true;
            condition = condition.and(
                searchFursonaQuery.field(USERS.USER_ID).notIn(
                    PostgresDSL.select(ROOM_GUESTS.USER_ID)
                    .from(ROOM_GUESTS)
                    .where(
                        ROOM_GUESTS.CONFIRMED.isTrue()
                    //TODO find users inside the same room of the current user
                    //.and(ROOM_GUESTS.ROOM_ID.notEqual())
                    )
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

        if (filerNotMadeAnOrder) {
            leftJoinOrders = true;
            condition = condition.and(
                ORDERS.ID.isNull()
            );
        }

        if (filterMembershipCardForYear != null) {
            joinMembershipCards = true;
            condition = condition.and(
                MEMBERSHIP_CARDS.ISSUE_YEAR.isNull()
                .or(MEMBERSHIP_CARDS.ISSUE_YEAR.notEqual(filterMembershipCardForYear))
            );
        }

        if (filterBanStatus != null) {
            joinBanStatus = true;
            condition = condition.and(
                AUTHENTICATIONS.AUTHENTICATION_DISABLED.eq(filterBanStatus)
            );
        }

        if (filterWithoutRole != null) {
            condition = condition.and(
                searchFursonaQuery.field(USERS.USER_ID).notIn(
                    PostgresDSL.select(USER_HAS_ROLE.USER_ID)
                    .from(USER_HAS_ROLE)
                    .innerJoin(ROLES)
                    .on(
                        USER_HAS_ROLE.ROLE_ID.eq(ROLES.ROLE_ID)
                        .and(ROLES.INTERNAL_NAME.eq(filterWithoutRole))
                    )
                )
            );
        }

        SelectJoinStep<?> query =
            PostgresDSL.selectDistinct(
                searchFursonaQuery.field(USERS.USER_ID),
                searchFursonaQuery.field(USERS.USER_FURSONA_NAME),
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID
            )
            .from(searchFursonaQuery)
            .leftJoin(MEDIA)
            .on(searchFursonaQuery.field(USERS.MEDIA_ID_PROPIC).eq(MEDIA.MEDIA_ID));

        if (joinBanStatus) {
            query = query
                .innerJoin(AUTHENTICATIONS)
                .on(searchFursonaQuery.field(USERS.USER_ID).eq(AUTHENTICATIONS.USER_ID));
        }

        if (joinOrders) {
            query = query
                .innerJoin(ORDERS)
                .on(
                    searchFursonaQuery.field(USERS.USER_ID).eq(ORDERS.USER_ID)
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                );
        }
        if (leftJoinOrders && !joinOrders) {
            query = query
                .leftJoin(ORDERS)
                .on(
                    searchFursonaQuery.field(USERS.USER_ID).eq(ORDERS.USER_ID)
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                );
        }

        if (joinMembershipCards) {
            query = query
                .leftJoin(MEMBERSHIP_CARDS)
                .on(searchFursonaQuery.field(USERS.USER_ID).eq(MEMBERSHIP_CARDS.USER_ID));
        }

        var finalQuery = query.where(condition).asTable("finalq");
        return sqlQuery.fetch(
            PostgresDSL.select(PostgresDSL.asterisk())
            .from(finalQuery)
            .orderBy(
                PostgresDSL.position(inputQuery, finalQuery.field(USERS.USER_FURSONA_NAME)),
                finalQuery.field(USERS.USER_FURSONA_NAME)
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

    @Override
    public SelectJoinStep<?> selectJoinDisplayUser(long eventId) {
        return selectDisplayUser()
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .leftJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(eventId))
            );
    }

    @Override
    public SelectJoinStep<?> selectDisplayUser() {
        return PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID,
                ORDERS.ORDER_SPONSORSHIP_TYPE
            )
            .from(USERS);
    }
}
