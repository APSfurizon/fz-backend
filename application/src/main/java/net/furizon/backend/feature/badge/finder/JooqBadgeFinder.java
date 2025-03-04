package net.furizon.backend.feature.badge.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.UserBadgePrint;
import net.furizon.backend.feature.badge.mapper.JooqUserBadgePrintMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.mapper.JooqMediaMapper;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.True;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.*;

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
    public @NotNull List<UserBadgePrint> getUserBadgesToPrint(
            @NotNull Event event,
            @Nullable String orderCodes,
            @Nullable String orderSerials,
            @Nullable String userIds) {
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

        List<UserBadgePrint> badges = sqlQuery.fetch(
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
        ).stream().map(JooqUserBadgePrintMapper::map).toList();

        Map<Long, UserBadgePrint> idToObj = badges.stream().collect(
            Collectors.toMap(
                UserBadgePrint::getUserId,
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

        return badges;
    }
}
