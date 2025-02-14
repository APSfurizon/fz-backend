package net.furizon.backend.feature.admin.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.admin.dto.CapabilitiesResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetCapabilitiesUseCase implements UseCase<FurizonUser, CapabilitiesResponse> {
    @NotNull private final PermissionFinder permissionFinder;

    @Override
    public @NotNull CapabilitiesResponse executor(@NotNull FurizonUser input) {
        Set<Permission> p = permissionFinder.getUserPermissions(input.getUserId());
        return CapabilitiesResponse.builder()
                .canUpgradeUser(p.contains(Permission.CAN_UPGRADE_USERS))
                .canBanUsers(p.contains(Permission.CAN_BAN_USERS))
                .canManageMembershipCards(p.contains(Permission.CAN_MANAGE_MEMBERSHIP_CARDS))
                .canRefreshPretixCache(p.contains(Permission.CAN_REFRESH_PRETIX_CACHE))
                .canRemindOrderLinking(p.contains(Permission.PRETIX_ADMIN))
                .canRemindBadgeUploads(p.contains(Permission.CAN_MANAGE_USER_PUBLIC_INFO))
                .build();
    }
}
