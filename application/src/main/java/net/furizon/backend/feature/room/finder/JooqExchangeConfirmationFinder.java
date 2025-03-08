package net.furizon.backend.feature.room.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.mapper.ExchangeConfirmationStatusMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record7;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public @NotNull List<ExchangeConfirmationStatus> getAllExchangesOfUserInEvent(long userId, @NotNull Event event) {
        return query.fetch(
                select()
                .where(
                    EXCHANGE_CONFIRMATION_STATUS.EVENT_ID.eq(event.getId())
                    .and(
                        EXCHANGE_CONFIRMATION_STATUS.SOURCE_USER_ID.eq(userId)
                        .or(EXCHANGE_CONFIRMATION_STATUS.TARGET_USER_ID.eq(userId))
                    )
                )
        ).stream().map(ExchangeConfirmationStatusMapper::map).toList();
    }

    @NotNull
    private SelectJoinStep<Record7<Long, Long, Long, Boolean, Boolean, Long, Short>> select() {
        return PostgresDSL.select(
                        EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID,
                        EXCHANGE_CONFIRMATION_STATUS.TARGET_USER_ID,
                        EXCHANGE_CONFIRMATION_STATUS.SOURCE_USER_ID,
                        EXCHANGE_CONFIRMATION_STATUS.TARGET_CONFIRMED,
                        EXCHANGE_CONFIRMATION_STATUS.SOURCE_CONFIRMED,
                        EXCHANGE_CONFIRMATION_STATUS.EVENT_ID,
                        EXCHANGE_CONFIRMATION_STATUS.ACTION_TYPE
                ).from(EXCHANGE_CONFIRMATION_STATUS);
    }
}
