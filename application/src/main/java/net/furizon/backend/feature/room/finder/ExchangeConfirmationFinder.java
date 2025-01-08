package net.furizon.backend.feature.room.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ExchangeConfirmationFinder {
    @Nullable
    ExchangeConfirmationStatus getExchangeStatusFromId(long exchangeId);
    @Nullable
    ExchangeConfirmationStatus getExchangeStatusFromSourceUsrIdEvent(long sourceUsrId, @NotNull Event event);
}
