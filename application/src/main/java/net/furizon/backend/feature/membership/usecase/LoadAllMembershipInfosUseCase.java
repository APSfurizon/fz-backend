package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.GetMembershipCardsResponse;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadAllMembershipInfosUseCase implements UseCase<Short, GetMembershipCardsResponse> {
    @NotNull private final MembershipCardFinder membershipCardFinder;

    @Override
    public @NotNull GetMembershipCardsResponse executor(@NotNull Short year) {
        log.info("Loading all membership cards for year {}", year);
        return new GetMembershipCardsResponse(membershipCardFinder.getMembershipCards(year));
    }
}
