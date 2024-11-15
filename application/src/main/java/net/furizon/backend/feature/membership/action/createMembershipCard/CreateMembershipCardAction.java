package net.furizon.backend.feature.membership.action.createMembershipCard;


import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface CreateMembershipCardAction {
    void invoke(long userId, @NotNull Event event);
}
