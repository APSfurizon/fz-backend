package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchUserInEventUseCase implements UseCase<SearchUserInEventUseCase.Input, SearchUsersResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull SearchUserInEventUseCase.Input input) {
        Event event = input.pretixService.getCurrentEvent();
        return new SearchUsersResponse(userFinder.searchUserInCurrentEvent(
                input.fursonaName,
                event,
                input.filterRoom,
                input.filterPaid,
                input.filterNotMadeAnOrder,
                input.filterMembershipForYear,
                input.banStatus,
                input.filterWithoutRole
        ));
    }

    public record Input(
            @NotNull String fursonaName,
            @NotNull PretixInformation pretixService,
            boolean filterRoom,
            boolean filterPaid,
            boolean filterNotMadeAnOrder,
            @Nullable Short filterMembershipForYear,
            @Nullable Boolean banStatus,
            @Nullable String filterWithoutRole
    ) {}
}
