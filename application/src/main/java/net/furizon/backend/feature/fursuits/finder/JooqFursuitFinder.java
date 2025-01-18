package net.furizon.backend.feature.fursuits.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.mapper.JooqFursuitDisplayMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
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
    public @NotNull List<FursuitDisplayData> getFursuitsOfUser(long userId, Event event) {
        return sqlQuery.fetch(
            selectDisplayFursuit(event)
            .where(FURSUITS.USER_ID.eq(userId))
        ).stream().map(JooqFursuitDisplayMapper::mapWithOrder).toList();
    }
    @Override
    public @Nullable FursuitDisplayData getFursuit(long fursuitId, Event event) {
        return sqlQuery.fetchFirst(
            selectDisplayFursuit(event)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ).mapOrNull(JooqFursuitDisplayMapper::mapWithOrder);
    }

    private @NotNull SelectOnConditionStep<?> selectDisplayFursuit(@NotNull Event event) {
        return PostgresDSL.select(
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_PATH,
                FURSUITS.FURSUIT_ID,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                ORDERS.ID
            )
            .from(FURSUITS)

            .leftJoin(MEDIA)
            .on(MEDIA.MEDIA_ID.eq(FURSUITS.MEDIA_ID_PROPIC))

            .leftJoin(ORDERS)
            .on(FURSUITS_ORDERS.FURSUIT_ID.eq(FURSUITS.FURSUIT_ID))
            .leftJoin(ORDERS)
            .on(
                FURSUITS_ORDERS.FURSUIT_ID.isNotNull()
                .and(FURSUITS_ORDERS.ORDER_ID.eq(ORDERS.ID))
                .and(ORDERS.EVENT_ID.eq(event.getId()))
            );
    }
}
