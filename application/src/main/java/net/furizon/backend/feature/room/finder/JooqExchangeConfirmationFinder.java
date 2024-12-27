package net.furizon.backend.feature.room.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.mapper.ExchangeConfirmationStatusMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.EXCHANGE_CONFIRMATION_STATUS;

@Component
@RequiredArgsConstructor
public class JooqExchangeConfirmationFinder implements ExchangeConfirmationFinder {
    @NotNull private final SqlQuery query;

    @Override
    @Nullable
    public ExchangeConfirmationStatus getExchangeStatusFromId(long exchangeId) {
        return query.fetchFirst(
                select()
                .where(EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID.eq(exchangeId))
        ).mapOrNull(ExchangeConfirmationStatusMapper::map);
    }

    @Override
    @Nullable
    public ExchangeConfirmationStatus getExchangeStatusFromSourceUsrIdEvent(long sourceUsrId, @NotNull Event event) {
        return query.fetchFirst(
                select()
                .where(
                    EXCHANGE_CONFIRMATION_STATUS.SOURCE_USER_ID.eq(sourceUsrId)
                    .and(EXCHANGE_CONFIRMATION_STATUS.EVENT_ID.eq(event.getId()))
                )
        ).mapOrNull(ExchangeConfirmationStatusMapper::map);
    }

    @NotNull
    private SelectJoinStep<Record6<Long, Long, Long, Boolean, Boolean, Long>> select() {
        return PostgresDSL.select(
                        EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID,
                        EXCHANGE_CONFIRMATION_STATUS.TARGET_USER_ID,
                        EXCHANGE_CONFIRMATION_STATUS.SOURCE_USER_ID,
                        EXCHANGE_CONFIRMATION_STATUS.TARGET_CONFIRMED,
                        EXCHANGE_CONFIRMATION_STATUS.SOURCE_CONFIRMED,
                        EXCHANGE_CONFIRMATION_STATUS.EVENT_ID
                ).from(EXCHANGE_CONFIRMATION_STATUS);
    }
}
