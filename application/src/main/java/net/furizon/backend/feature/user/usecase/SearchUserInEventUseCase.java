package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchUserInEventUseCase implements UseCase<SearchUserInEventUseCase.Input, SearchUsersResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull SearchUserInEventUseCase.Input input) {
        return new SearchUsersResponse(userFinder.searchUserInCurrentEvent(
                input.fursonaName,
                input.pretixService.getCurrentEvent(),
                input.filterRoom
        ));
    }

    public record Input(
            @NotNull String fursonaName,
            @NotNull PretixInformation pretixService,
            boolean filterRoom
    ) {}
}
