package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithPersonalInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MembershipCardFinder {
    int countCardsPerUserPerEvent(long userId, @NotNull Event event);
    @Nullable MembershipCard getMembershipCardByOrderId(long orderId);
    @Nullable MembershipCard getMembershipCardByCardId(long cardId);

    @NotNull List<FullInfoMembershipCard> getMembershipCards(short year);
    @NotNull List<FullInfoMembershipCard> getMembershipCards(short year, int afterCardNo);

    @NotNull List<UserDisplayDataWithPersonalInfo> getUsersAtEventWithoutMembershipCard(@NotNull Event event);

    @NotNull List<MembershipCard> getCardsOfUser(long userId);

    boolean canDeleteCard(@NotNull MembershipCard card);
}
