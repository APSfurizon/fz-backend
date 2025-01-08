package net.furizon.backend.feature.room.action.deleteExpiredExchange;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.EXCHANGE_CONFIRMATION_STATUS;

@Component
@RequiredArgsConstructor
public class JooqDeleteExpiredExchangesAction implements DeleteExpiredExchangesAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public int invoke() {
        return sqlCommand.execute(
                PostgresDSL.deleteFrom(EXCHANGE_CONFIRMATION_STATUS)
                .where(EXCHANGE_CONFIRMATION_STATUS.EXPIRES_ON.lessOrEqual(System.currentTimeMillis()))
        );
    }
}
