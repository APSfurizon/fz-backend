package net.furizon.backend.feature.pretix.objects.order.action.pushAnswer;

import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;

public interface PushPretixAnswerAction {
    boolean invoke(
        @NotNull final Order order,
        @NotNull final PretixInformation pretixInformation
    );
}
