package net.furizon.backend.infrastructure.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum Permission {
    CAN_UPGRADE_USERS(0L),
    CAN_BAN_USERS(1L),
    CAN_SEE_ADMIN_PAGES(2L),
    PRETIX_ADMIN(3L),
    CAN_MANAGE_USER_PUBLIC_INFO(4L),
    CAN_VIEW_USER(5L),
    CAN_MANAGE_ROOMS(100L),
    CAN_MANAGE_MEMBERSHIP_CARDS(101L),
    CAN_REFRESH_PRETIX_CACHE(102L),
    CAN_MANAGE_RAW_UPLOADS(103L),
    EARLY_BOOK(10000L);

    // We use an external value instead of .ordinal() so we can rearrange and move
    // the code freely, without destroying existing permissions
    private final long value;

    private static final Map<Long, Permission> PERMISSIONS = new HashMap<>();

    static {
        for (final Permission permission : Permission.values()) {
            long permissionValue = permission.value;
            if (PERMISSIONS.containsKey(permissionValue)) {
                throw new RuntimeException("Duplicate permission id: " + permissionValue);
            }
            PERMISSIONS.put(permissionValue, permission);
        }
    }

    @Nullable
    public static Permission get(final long value) {
        return PERMISSIONS.get(value);
    }
}
