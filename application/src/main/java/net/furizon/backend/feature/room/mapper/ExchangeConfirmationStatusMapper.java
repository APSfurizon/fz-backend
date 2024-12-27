package net.furizon.backend.feature.room.mapper;

import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.ExchangeConfirmationStatus.EXCHANGE_CONFIRMATION_STATUS;

public class ExchangeConfirmationStatusMapper {
    public static ExchangeConfirmationStatus map(Record record) {
        return ExchangeConfirmationStatus.builder()
                .exchangeId(record.get(EXCHANGE_CONFIRMATION_STATUS.EXCHANGE_ID))
                .targetUserId(record.get(EXCHANGE_CONFIRMATION_STATUS.TARGET_USER_ID))
                .sourceUserId(record.get(EXCHANGE_CONFIRMATION_STATUS.SOURCE_USER_ID))
                .targetConfirmed(record.get(EXCHANGE_CONFIRMATION_STATUS.TARGET_CONFIRMED))
                .sourceConfirmed(record.get(EXCHANGE_CONFIRMATION_STATUS.SOURCE_CONFIRMED))
                .eventId(record.get(EXCHANGE_CONFIRMATION_STATUS.EVENT_ID))
                .build();
    }
}
