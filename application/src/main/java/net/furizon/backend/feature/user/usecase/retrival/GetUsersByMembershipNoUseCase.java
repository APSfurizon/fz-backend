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
public class GetUsersByMembershipNoUseCase implements
        UseCase<GetUsersByMembershipNoUseCase.Input, SearchUsersResponse> {
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull GetUsersByMembershipNoUseCase.Input input) {
        final Set<String> parsedNos = Set.of(input.membershipNumbers);
        final List<UserDisplayData> result = userFinder.getDisplayUserByMembershipNo(parsedNos, input.event);
        return new SearchUsersResponse(result.stream().map(data ->
                new SearchUserResult(data.getUserId(), data.getFursonaName(), data.getPropic()))
                .toList());
    }

    public record Input(
            String[] membershipNumbers,
            @NotNull Event event
    ) {}
}
