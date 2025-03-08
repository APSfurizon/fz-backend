package net.furizon.backend.infrastructure.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum Permission {
    //General and super permissions
    CAN_UPGRADE_USERS(0L),
    CAN_BAN_USERS(1L),
    CAN_SEE_ADMIN_PAGES(2L),
    PRETIX_ADMIN(3L),

    // Web admin specific permissions
    CAN_MANAGE_ROOMS(100L),
    CAN_MANAGE_MEMBERSHIP_CARDS(101L),
    CAN_REFRESH_PRETIX_CACHE(102L),
    CAN_MANAGE_RAW_UPLOADS(103L),
    CAN_MANAGE_USER_PUBLIC_INFO(104L),
    CAN_VIEW_USER(105L),

    //Normal user permissions
    EARLY_BOOK(10000L),
    JUNIOR_STAFF(10001L),
    MAIN_STAFF(10002L),

    UNKNOWN_PERMISSION(Long.MAX_VALUE),
    ;

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

    @NotNull
    public static Permission get(final long value) {
        Permission p = PERMISSIONS.get(value);
        if (p == null) {
            p = UNKNOWN_PERMISSION;
        }
        return p;
    }
}
