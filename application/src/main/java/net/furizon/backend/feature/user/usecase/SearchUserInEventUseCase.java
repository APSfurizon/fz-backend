package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchUserInEventUseCase implements UseCase<SearchUserInEventUseCase.Input, SearchUsersResponse> {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull SearchUsersResponse executor(@NotNull SearchUserInEventUseCase.Input input) {
        Event event = input.pretixService.getCurrentEvent();
        boolean isAdminSearch = input.isAdminSearch;
        if (isAdminSearch) {
            Set<Permission> permissions = permissionFinder.getUserPermissions(input.user.getUserId());
            if (!permissions.contains(Permission.CAN_SEE_ADMIN_PAGES)) {
                log.warn("User {} had isAdminSearch=true, but he doesn't have the correct permissions",
                        input.user.getUserId());
                isAdminSearch = false;
            }
        }
        return new SearchUsersResponse(userFinder.searchUserInCurrentEvent(
                input.inputQuery,
                isAdminSearch,
                event,
                input.filterRoom,
                input.filterPaid,
                input.filterNotMadeAnOrder,
                input.filterMembershipForYear,
                input.banStatus
        ));
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull String inputQuery,
            boolean isAdminSearch,
            @NotNull PretixInformation pretixService,
            boolean filterRoom,
            boolean filterPaid,
            boolean filterNotMadeAnOrder,
            @Nullable Short filterMembershipForYear,
            @Nullable Boolean banStatus
    ) {}
}
