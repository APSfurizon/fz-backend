package net.furizon.backend.feature.room.action.deleteExchangeStatusObjAction;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.EXCHANGE_CONFIRMATION_STATUS;

@Component
@RequiredArgsConstructor
public class JooqDeleteExchangeStatusObjAction implements DeleteExchangeStatusObjAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long exchangeId) {
        return sqlCommand.execute(
                PostgresDSL.deleteFrom(EXCHANGE_CONFIRMATION_STATUS)
                .where(EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID.eq(exchangeId))
        ) > 0;
    }
}
