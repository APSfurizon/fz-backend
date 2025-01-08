package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.GetMembershipCardsResponse;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

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
        short eventYear = input.event.getMembershipYear(membershipYearUtils);
        return new GetMembershipCardsResponse(membershipCardFinder.getMembershipCards(input.year),
                input.year.equals(eventYear));
    }

    public record Input(Short year, @NotNull Event event) {}
}
