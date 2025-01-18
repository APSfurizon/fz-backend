package net.furizon.backend.feature.user.usecase;

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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetSelectedUsersByIdsUseCase implements UseCase<GetSelectedUsersByIdsUseCase.Input, SearchUsersResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull GetSelectedUsersByIdsUseCase.Input input) {
        final Set<Long> parsedIds = Arrays.stream(input.userIds).map(Long::parseLong).collect(Collectors.toSet());
        final List<UserDisplayData> result = userFinder.getDisplayUserByIds(parsedIds, input.event);
        return new SearchUsersResponse(result.stream().map(data ->
                new SearchUserResult(data.getUserId(), data.getFursonaName(), data.getPropic()))
                .toList());
    }

    public record Input(
            @NotNull String[] userIds,
            @NotNull Event event
    ) {}
}
