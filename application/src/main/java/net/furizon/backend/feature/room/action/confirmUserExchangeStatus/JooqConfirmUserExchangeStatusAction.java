package net.furizon.backend.feature.room.action.confirmUserExchangeStatus;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.EXCHANGE_CONFIRMATION_STATUS;

@Component
@RequiredArgsConstructor
public class JooqConfirmUserExchangeStatusAction implements ConfirmUserExchangeStatusAction {
    @NotNull private final SqlCommand sqlCommand;


    @Override
    public boolean invoke(boolean isSourceUser, long exchangeId) {
        return sqlCommand.execute(
                PostgresDSL.update(EXCHANGE_CONFIRMATION_STATUS)
                .set(
                        isSourceUser
                                ? EXCHANGE_CONFIRMATION_STATUS.SOURCE_CONFIRMED
                                : EXCHANGE_CONFIRMATION_STATUS.TARGET_CONFIRMED,
                        true
                )
        ) > 0;
    }
}
