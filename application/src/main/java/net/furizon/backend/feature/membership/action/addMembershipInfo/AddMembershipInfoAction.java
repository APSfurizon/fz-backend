package net.furizon.backend.feature.membership.action.addMembershipInfo;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;

public interface AddMembershipInfoAction {
    void invoke(long userId, @NotNull final PersonalUserInformation personalUserInformation);
}
