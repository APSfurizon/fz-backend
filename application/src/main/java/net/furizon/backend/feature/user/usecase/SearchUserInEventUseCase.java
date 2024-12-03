package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchUserInEventUseCase implements UseCase<SearchUserInEventUseCase.Input, SearchUsersResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull SearchUserInEventUseCase.Input input) {
        var e = input.pretixService.getCurrentEvent();
        if (e.isPresent()) {
            return new SearchUsersResponse(userFinder.searchUserInCurrentEvent(
                    input.fursonaName,
                    e.get(),
                    input.filterRoom
            ));
        } else {
            log.warn("Current event is null!");
            return new SearchUsersResponse(new LinkedList<>());
        }
    }

    public record Input(
            @NotNull String fursonaName,
            @NotNull PretixInformation pretixService,
            boolean filterRoom
    ) {}
}
