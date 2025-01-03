package net.furizon.backend.feature.room.action.createExchangeConfirmationStatusObj;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.EXCHANGE_CONFIRMATION_STATUS;

@Component
@RequiredArgsConstructor
public class JooqCreateExchangeObjAction implements CreateExchangeObjAction {
    @NotNull private final SqlCommand sqlCommand;

    @Value("${room.exchanges.expire-after-ms}")
    private long expireAfterMs;

    @Override
    public long invoke(long targetUserId, long sourceUserId, @NotNull ExchangeAction action, @NotNull Event event) {
        return sqlCommand.executeResult(
                PostgresDSL.insertInto(
                        EXCHANGE_CONFIRMATION_STATUS,
                        EXCHANGE_CONFIRMATION_STATUS.TARGET_USER_ID,
                        EXCHANGE_CONFIRMATION_STATUS.SOURCE_USER_ID,
                        EXCHANGE_CONFIRMATION_STATUS.EVENT_ID,
                        EXCHANGE_CONFIRMATION_STATUS.EXPIRES_ON
                ).values(
                        targetUserId,
                        sourceUserId,
                        event.getId(),
                        System.currentTimeMillis() + expireAfterMs
                ).returning(
                        EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID
                )
        ).getFirst().get(EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID);
    }
}
