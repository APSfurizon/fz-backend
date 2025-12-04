package net.furizon.backend.feature.room.action.exchangeRoom;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.ExchangeRoomRequest;
import org.jetbrains.annotations.NotNull;

public interface ExchangeRoomOnPretixAction {
    boolean invoke(@NotNull ExchangeRoomRequest exchange, @NotNull Event event);
}
