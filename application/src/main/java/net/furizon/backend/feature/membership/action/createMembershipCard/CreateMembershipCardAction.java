package net.furizon.backend.feature.membership.action.createMembershipCard;


import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import org.jetbrains.annotations.Nullable;

public interface CreateMembershipCardAction {
    void invoke(long userId, @Nullable Event event, @Nullable Order order);
}
