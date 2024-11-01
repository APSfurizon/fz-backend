package net.furizon.backend.feature.pretix.order.action.addAnswer;

import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import org.jetbrains.annotations.NotNull;

public interface AddPretixAnswerAction {
    boolean invoke(
        @NotNull final Order order,
        @NotNull final Event.OrganizerAndEventPair pair
    );
}
