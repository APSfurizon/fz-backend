package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface MembershipCardFinder {
    int countCardsPerUserPerEvent(long userId, @NotNull Event event);
}
