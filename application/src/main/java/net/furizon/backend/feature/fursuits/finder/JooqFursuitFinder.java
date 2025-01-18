package net.furizon.backend.feature.fursuits.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.mapper.JooqFursuitDisplayMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectOnConditionStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.tables.Media.MEDIA;
import static net.furizon.jooq.generated.tables.Orders.ORDERS;
import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;
import static net.furizon.jooq.generated.tables.FursuitsOrders.FURSUITS_ORDERS;

@Component
@RequiredArgsConstructor
public class JooqFursuitFinder implements FursuitFinder {
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public @NotNull List<FursuitDisplayData> getFursuitsOfUser(long userId, @Nullable Event event) {
        return sqlQuery.fetch(
            selectDisplayFursuit(event)
            .where(FURSUITS.USER_ID.eq(userId))
        ).stream().map(r -> JooqFursuitDisplayMapper.map(r, event != null)).toList();
    }
    @Override
    public @Nullable FursuitDisplayData getFursuit(long fursuitId, @Nullable Event event) {
        return sqlQuery.fetchFirst(
            selectDisplayFursuit(event)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ).mapOrNull(r -> JooqFursuitDisplayMapper.map(r, event != null));
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

    private @NotNull SelectOnConditionStep<?> selectDisplayFursuit(@Nullable Event event) {
        var q = PostgresDSL.select(
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_PATH,
                FURSUITS.FURSUIT_ID,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                FURSUITS.USER_ID,
                ORDERS.ID
            )
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
