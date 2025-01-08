package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UsersByIdResponse;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchUsersByIdsUseCase implements UseCase<SearchUsersByIdsUseCase.Input, UsersByIdResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull UsersByIdResponse executor(@NotNull SearchUsersByIdsUseCase.Input input) {
        final Set<Long> parsedIds = Arrays.stream(input.userIds).map(Long::parseLong).collect(Collectors.toSet());
        return new UsersByIdResponse(userFinder.getDisplayUserByIds(parsedIds, input.event));
    }

    public record Input(
            @NotNull String[] userIds,
            @NotNull Event event
    ) {}
}
