package net.furizon.backend.infrastructure.security;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralChecks {
    @NotNull private final PermissionFinder permissionFinder;

    public long getUserIdAndAssertPermission(long requesterUserId, @Nullable Long userId) {
        long res = requesterUserId;
        if (userId != null && userId != requesterUserId) {
            boolean p = permissionFinder.userHasPermission(requesterUserId, Permission.CAN_MANAGE_USER_PUBLIC_INFO);
            if (!p) {
                throw new ApiException("Birichino :)", SecurityResponseCodes.USER_IS_NOT_ADMIN);
            }
            res = userId;
        }
        return res;
    }
}
