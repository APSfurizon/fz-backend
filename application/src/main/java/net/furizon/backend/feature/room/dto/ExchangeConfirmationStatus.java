package net.furizon.backend.feature.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.room.action.confirmUserExchangeStatus.ConfirmUserExchangeStatusAction;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class ExchangeConfirmationStatus {
    private long exchangeId;

    private final long targetUserId;
    private final long sourceUserId;
    private boolean targetConfirmed;
    private boolean sourceConfirmed;

    private final long eventId;

    private final ExchangeAction action;

    public boolean confirmUser(boolean isSourceUser, @NotNull ConfirmUserExchangeStatusAction confirmUserExchangeStatusAction) {
        if (confirmUserExchangeStatusAction.invoke(isSourceUser, exchangeId)) {
            if (isSourceUser) {
                targetConfirmed = true;
            } else {
                sourceConfirmed = true;
            }
        }
        return isFullyConfirmed();
    }

    public boolean isFullyConfirmed() {
        return targetConfirmed && sourceConfirmed;
    }
}
