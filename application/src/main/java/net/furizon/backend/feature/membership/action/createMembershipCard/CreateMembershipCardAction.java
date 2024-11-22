package net.furizon.backend.feature.membership.action.createMembershipCard;


import jakarta.validation.constraints.Null;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CreateMembershipCardAction {
    void invoke(long userId, @Nullable Event event);
}
