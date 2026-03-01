package net.furizon.backend.feature.user.usecase.retrival;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.feature.user.objects.SearchUserResult;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUsersByFursuitIdUseCase implements UseCase<GetUsersByFursuitIdUseCase.Input, SearchUsersResponse> {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull GetUsersByFursuitIdUseCase.Input input) {
        final Set<Long> parsedIds = Set.of(input.fursuitIds);
        boolean isAdmin = permissionFinder.userHasPermission(input.user.getUserId(), Permission.CAN_SEE_ADMIN_PAGES);
        final List<UserDisplayData> result = userFinder.getDisplayUserByFursuitIds(parsedIds, input.event, !isAdmin);
        return new SearchUsersResponse(result.stream().map(data ->
                new SearchUserResult(data.getUserId(), data.getFursonaName(), data.getPropic()))
                .toList());
    }

    public record Input(
            Long[] fursuitIds,
            @NotNull Event event,
            FurizonUser user
    ) {}
}
