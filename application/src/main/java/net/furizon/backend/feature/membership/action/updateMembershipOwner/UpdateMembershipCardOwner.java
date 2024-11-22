package net.furizon.backend.feature.membership.action.updateMembershipOwner;


import net.furizon.backend.feature.membership.dto.MembershipCard;
import org.jetbrains.annotations.NotNull;

public interface UpdateMembershipCardOwner {

    void invoke(@NotNull MembershipCard card, long newOwnerId);
}
