package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;
import net.furizon.backend.feature.membership.dto.GetMembershipCardsResponse;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithOrderCode;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadAllMembershipInfosUseCase implements UseCase<LoadAllMembershipInfosUseCase.Input,
        GetMembershipCardsResponse> {
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final MembershipYearUtils membershipYearUtils;

    @Override
    public @NotNull GetMembershipCardsResponse executor(@NotNull Input input) {
        log.info("Loading all membership cards for year {}", input.year);
        short eventYear = input.currentEvent.getMembershipYear(membershipYearUtils);
        boolean isCurrentYear = input.year == eventYear;
        Long currentEventId = isCurrentYear ? input.currentEvent.getId() : null;
        List<FullInfoMembershipCard> cards = membershipCardFinder.getMembershipCards(input.year, currentEventId);

        //Mark duplicates
        Map<Long, FullInfoMembershipCard> userToCard = new HashMap<>();
        for (FullInfoMembershipCard card : cards) {
            long userId = card.getUser().getUserId();

            FullInfoMembershipCard savedCard = userToCard.get(userId);
            if (savedCard != null) {
                savedCard.setDuplicate(true);
                card.setDuplicate(true);
            } else {
                userToCard.put(userId, card);
            }
        }

        List<UserDisplayDataWithOrderCode> peopleWithNoCards = !isCurrentYear ? null :
                membershipCardFinder.getUsersAtEventWithoutMembershipCard(input.currentEvent);

        return new GetMembershipCardsResponse(cards, peopleWithNoCards, isCurrentYear);
    }

    public record Input(short year, @NotNull Event currentEvent) {}
}
