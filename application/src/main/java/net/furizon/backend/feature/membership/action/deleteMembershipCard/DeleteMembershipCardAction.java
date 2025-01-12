package net.furizon.backend.feature.membership.action.deleteMembershipCard;

import net.furizon.backend.feature.membership.dto.MembershipCard;
import org.jetbrains.annotations.NotNull;

public interface DeleteMembershipCardAction {
    boolean invoke(@NotNull MembershipCard card);
}
