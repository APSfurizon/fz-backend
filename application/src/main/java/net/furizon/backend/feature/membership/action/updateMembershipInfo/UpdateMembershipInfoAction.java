package net.furizon.backend.feature.membership.action.updateMembershipInfo;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;

public interface UpdateMembershipInfoAction {
    void invoke(long userId, @NotNull PersonalUserInformation personalUserInformation, @NotNull Event event);
}
