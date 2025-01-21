package net.furizon.backend.feature.nosecount.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.mapper.JooqFursuitDisplayMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;
import static net.furizon.jooq.generated.tables.Media.MEDIA;
import static net.furizon.jooq.generated.tables.Orders.ORDERS;
import static net.furizon.jooq.generated.tables.FursuitsOrders.FURSUITS_ORDERS;

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
        ).stream().map(JooqFursuitDisplayMapper::mapWithOrder).toList();
    }
}
