package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface MembershipCardFinder {
    int countCardsPerUserPerEvent(long userId, @NotNull Event event);
    //UserId is mandatory if the ownership of an order changes
    boolean checkIfMembershipCardIsCreatedByOrder(long userId, long orderId);
}
