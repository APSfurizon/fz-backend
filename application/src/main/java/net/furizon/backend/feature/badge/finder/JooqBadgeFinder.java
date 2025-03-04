package net.furizon.backend.feature.badge.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.BadgeToPrint;
import net.furizon.backend.feature.badge.mapper.JooqBadgeToPrintMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.mapper.JooqMediaMapper;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.FURSUITS;
import static net.furizon.jooq.generated.Tables.FURSUITS_ORDERS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.PERMISSION;
import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqBadgeFinder implements BadgeFinder {
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public @Nullable MediaData getMediaDataOfUserBadge(long userId) {
        return sqlQuery.fetchFirst(
            mediaFinder.selectMedia()
            .join(USERS)
            .on(
                USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID)
                .and(USERS.USER_ID.eq(userId))
            )
        ).mapOrNull(JooqMediaMapper::map);
    }

    @Override
    public @Nullable MediaData getMediaDataOfFursuitBadge(long fursuitId) {
        return sqlQuery.fetchFirst(
            mediaFinder.selectMedia()
            .join(FURSUITS)
            .on(
                FURSUITS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID)
                .and(FURSUITS.FURSUIT_ID.eq(fursuitId))
            )
        ).mapOrNull(JooqMediaMapper::map);
    }

    @Override
    public @NotNull List<BadgeToPrint> getUserBadgesToPrint(
            @NotNull Event event,
            @Nullable String orderCodes,
            @Nullable String orderSerials,
            @Nullable String userIds) {

        Condition searchFilter = buildBadgesToPrintFilter(
                orderCodes,
                orderSerials,
                userIds,
                null
        );

        List<BadgeToPrint> badges = sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                ORDERS.ORDER_SERIAL_IN_EVENT,
                ORDERS.ORDER_CODE,
                ORDERS.ORDER_SPONSORSHIP_TYPE
            )
            .from(USERS)
            .innerJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .where(searchFilter)
            .orderBy(ORDERS.ORDER_SERIAL_IN_EVENT)
        ).stream().map(JooqBadgeToPrintMapper::mapUser).toList();

        addPermissionsToBadgesToPrint(badges);
        return badges;
    }

    @Override
    public @NotNull List<BadgeToPrint> getFursuitBadgesToPrint(
            @NotNull Event event,
            @Nullable String orderCodes,
            @Nullable String orderSerials,
            @Nullable String userIds,
            @Nullable String fursuitIds) {

        Condition searchFilter = buildBadgesToPrintFilter(
                orderCodes,
                orderSerials,
                userIds,
                fursuitIds
        );

        List<BadgeToPrint> badges = sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                ORDERS.ORDER_SERIAL_IN_EVENT,
                ORDERS.ORDER_CODE,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                FURSUITS.FURSUIT_ID,
                FURSUITS.FURSUIT_NAME
            )
            .from(USERS)
            .innerJoin(ORDERS)
            .on(
                USERS.USER_ID.eq(ORDERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            )
            .innerJoin(FURSUITS_ORDERS)
            .on(FURSUITS_ORDERS.ORDER_ID.eq(ORDERS.ID))
            .innerJoin(FURSUITS)
            .on(FURSUITS.FURSUIT_ID.eq(FURSUITS_ORDERS.FURSUIT_ID))
            .leftJoin(MEDIA)
            .on(FURSUITS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .where(searchFilter)
            .orderBy(ORDERS.ORDER_SERIAL_IN_EVENT)
        ).stream().map(JooqBadgeToPrintMapper::mapFursuit).toList();

        addPermissionsToBadgesToPrint(badges);
        return badges;
    }

    private Condition buildBadgesToPrintFilter(@Nullable String orderCodes,
                                               @Nullable String orderSerials,
                                               @Nullable String userIds,
                                               @Nullable String fursuitIds) {
        Condition searchFilter = PostgresDSL.trueCondition();

        if (orderCodes != null) {
            Condition condition = PostgresDSL.falseCondition();
            for (String code : orderCodes.split(",")) {
                condition = condition.or(ORDERS.ORDER_CODE.eq(code));
            }
            searchFilter = searchFilter.and(condition);
        }

        if (orderSerials != null) {
            Condition condition = PostgresDSL.falseCondition();
            for (String serial : orderSerials.split(",")) {
                String[] sp = serial.split("-");
                long i = Long.parseLong(sp[0]);
                if (sp.length > 1) {
                    condition = condition.or(
                        ORDERS.ORDER_SERIAL_IN_EVENT.greaterOrEqual(i)
                        .and(ORDERS.ORDER_SERIAL_IN_EVENT.lessOrEqual(Long.parseLong(sp[1])))
                    );
                } else {
                    condition = condition.or(ORDERS.ORDER_SERIAL_IN_EVENT.eq(i));
                }
            }
            searchFilter = searchFilter.and(condition);
        }

        if (userIds != null) {
            Condition condition = PostgresDSL.falseCondition();
            for (String id : userIds.split(",")) {
                String[] sp = id.split("-");
                long i = Long.parseLong(sp[0]);
                if (sp.length > 1) {
                    condition = condition.or(
                        USERS.USER_ID.greaterOrEqual(i)
                        .and(USERS.USER_ID.lessOrEqual(Long.parseLong(sp[1])))
                    );
                } else {
                    condition = condition.or(USERS.USER_ID.eq(i));
                }
            }
            searchFilter = searchFilter.and(condition);
        }

        if (fursuitIds != null) {
            Condition condition = PostgresDSL.falseCondition();
            for (String id : fursuitIds.split(",")) {
                String[] sp = id.split("-");
                long i = Long.parseLong(sp[0]);
                if (sp.length > 1) {
                    condition = condition.or(
                        FURSUITS.FURSUIT_ID.greaterOrEqual(i)
                        .and(FURSUITS.FURSUIT_ID.lessOrEqual(Long.parseLong(sp[1])))
                    );
                } else {
                    condition = condition.or(FURSUITS.FURSUIT_ID.eq(i));
                }
            }
            searchFilter = searchFilter.and(condition);
        }

        return searchFilter;
    }

    private void addPermissionsToBadgesToPrint(List<BadgeToPrint> badges) {
        Map<Long, BadgeToPrint> idToObj = badges.stream().collect(
            Collectors.toMap(
                BadgeToPrint::getUserId,
                Function.identity()
            )
        );
        Set<Long> fetchedUserIds = idToObj.keySet();
        sqlQuery.fetch(
            PostgresDSL.selectDistinct(
                USER_HAS_ROLE.USER_ID,
                PERMISSION.PERMISSION_VALUE
            )
            .from(USER_HAS_ROLE)
            .innerJoin(PERMISSION)
            .on(
                PERMISSION.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID)
                .and(USER_HAS_ROLE.USER_ID.in(fetchedUserIds))
            )
        ).forEach(r ->
            idToObj.get(r.get(USER_HAS_ROLE.USER_ID))
            .getPermissions()
            .add(Permission.get(r.get(PERMISSION.PERMISSION_VALUE)))
        );
    }
}
