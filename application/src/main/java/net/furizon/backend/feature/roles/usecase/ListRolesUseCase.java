package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.roles.dto.ListingRolesResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListRolesUseCase implements UseCase<FurizonUser, ListingRolesResponse> {
    @NotNull private final PermissionFinder permissionFinder;

    @Override
    public @NotNull ListingRolesResponse executor(@NotNull FurizonUser user) {
        log.info("User {} is fetching list of roles", user.getUserId());
        return new ListingRolesResponse(permissionFinder.listPermissions());
    }
}
