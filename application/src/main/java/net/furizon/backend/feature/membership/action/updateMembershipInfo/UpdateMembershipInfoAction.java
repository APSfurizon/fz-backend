package net.furizon.backend.feature.membership.action.updateMembershipInfo;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;

public interface UpdateMembershipInfoAction {
    void invoke(long userId, @NotNull PersonalUserInformation personalUserInformation);
}
