package net.furizon.backend.feature.fursuits.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.mapper.JooqFursuitDataMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.FURSUITS;
import static net.furizon.jooq.generated.Tables.FURSUITS_ORDERS;

@Component
@RequiredArgsConstructor
public class JooqFursuitFinder implements FursuitFinder {
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public @NotNull List<FursuitData> getFursuitsOfUser(long userId, @Nullable Event event) {
        return sqlQuery.fetch(
            selectDisplayFursuit(event)
            .where(FURSUITS.USER_ID.eq(userId))
            .orderBy(FURSUITS.FURSUIT_ID)
        ).stream().map(r -> JooqFursuitDataMapper.map(r, event != null, true)).toList();
    }
    @Override
    public @Nullable FursuitData getFursuit(long fursuitId, @Nullable Event event, boolean isOwner) {
        return sqlQuery.fetchFirst(
            selectDisplayFursuit(event)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ).mapOrNull(r -> JooqFursuitDataMapper.map(r, event != null, isOwner));
    }

    @Override
    public @NotNull List<FursuitData> getFursuitsWithoutPropic(@NotNull Event event) {
        return sqlQuery.fetch(
            selectDisplayFursuit(null) //Event = null so we manually INNER join the order
            .innerJoin(FURSUITS_ORDERS)
            .on(FURSUITS_ORDERS.FURSUIT_ID.eq(FURSUITS.FURSUIT_ID))
            .innerJoin(ORDERS)
            .on(
                FURSUITS_ORDERS.ORDER_ID.eq(ORDERS.ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
                .and(ORDERS.ORDER_STATUS.eq((short) OrderStatus.PAID.ordinal()))
            )
            .where(FURSUITS.MEDIA_ID_PROPIC.isNull())
        ).stream().map(r -> JooqFursuitDataMapper.map(r, false, false)).toList();
    }

    @Override
    public int countFursuitsOfUser(long userId) {
        return sqlQuery.count(
            PostgresDSL.select(FURSUITS.FURSUIT_ID)
            .from(FURSUITS)
            .where(FURSUITS.USER_ID.eq(userId))
        );
    }

    @Override
    public int countFursuitOfUserToEvent(long userId, @NotNull Order order) {
        return sqlQuery.count(
            PostgresDSL.select(FURSUITS.FURSUIT_ID)
            .from(FURSUITS)
            .innerJoin(FURSUITS_ORDERS)
            .on(
                FURSUITS.FURSUIT_ID.eq(FURSUITS_ORDERS.FURSUIT_ID)
                .and(FURSUITS_ORDERS.ORDER_ID.eq(order.getId()))
                .and(FURSUITS.USER_ID.eq(userId))
            )
        );
    }

    @Override
    public @Nullable Long getFursuitOwner(long fursuitId) {
        return sqlQuery.fetchFirst(
            PostgresDSL.select(FURSUITS.USER_ID)
            .from(FURSUITS)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ).mapOrNull(r -> r.get(FURSUITS.USER_ID));
    }

    @Override
    public boolean isFursuitBroughtToEvent(long fursuitId, @NotNull Order order) {
        return sqlQuery.fetchFirst(
            PostgresDSL.select(FURSUITS_ORDERS.FURSUIT_ID)
            .from(FURSUITS_ORDERS)
            .where(
                FURSUITS_ORDERS.FURSUIT_ID.eq(fursuitId)
                .and(FURSUITS_ORDERS.ORDER_ID.eq(order.getId()))
            )
        ).isPresent();
    }

    private @NotNull SelectOnConditionStep<?> selectDisplayFursuit(@Nullable Event event) {
        SelectSelectStep<?> sel = event == null
            ? PostgresDSL.select(
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_PATH,
                FURSUITS.FURSUIT_ID,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                FURSUITS.SHOW_IN_FURSUITCOUNT,
                FURSUITS.SHOW_OWNER,
                FURSUITS.USER_ID
            ) : PostgresDSL.select(
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_PATH,
                FURSUITS.FURSUIT_ID,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                FURSUITS.SHOW_IN_FURSUITCOUNT,
                FURSUITS.SHOW_OWNER,
                FURSUITS.USER_ID,
                ORDERS.ID,
                ORDERS.ORDER_SPONSORSHIP_TYPE
            );

        var q = sel
            .from(FURSUITS)

            .leftJoin(MEDIA)
            .on(MEDIA.MEDIA_ID.eq(FURSUITS.MEDIA_ID_PROPIC));

        if (event != null) {
            q = q.leftJoin(FURSUITS_ORDERS)
                .on(FURSUITS_ORDERS.FURSUIT_ID.eq(FURSUITS.FURSUIT_ID))
                .leftJoin(ORDERS)
                .on(
                    FURSUITS_ORDERS.FURSUIT_ID.isNotNull()
                    .and(FURSUITS_ORDERS.ORDER_ID.eq(ORDERS.ID))
                    .and(ORDERS.EVENT_ID.eq(event.getId()))
                );
        }

        return q;
    }
}
