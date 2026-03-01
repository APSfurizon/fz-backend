package net.furizon.backend.feature.user.usecase.retrival;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.feature.user.objects.SearchUserResult;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUsersByUserIdUseCase implements UseCase<GetUsersByUserIdUseCase.Input, SearchUsersResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull GetUsersByUserIdUseCase.Input input) {
        final Set<Long> parsedIds = Set.of(input.userIds);
        final List<UserDisplayData> result = userFinder.getDisplayUserByIds(parsedIds, input.event);
        return new SearchUsersResponse(result.stream().map(data ->
                new SearchUserResult(data.getUserId(), data.getFursonaName(), data.getPropic()))
                .toList());
    }

    public record Input(
            Long[] userIds,
            @NotNull Event event
    ) {}
}
