package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MembershipCardFinder {
    int countCardsPerUserPerEvent(long userId, @NotNull Event event);
    List<FullInfoMembershipCard> getMembershipCards(short year);
    @Nullable MembershipCard getMembershipCardByOrderId(long orderId);
    @Nullable MembershipCard getMembershipCardByCardId(long cardId);
}
