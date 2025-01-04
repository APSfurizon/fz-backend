package net.furizon.backend.infrastructure.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum Permission {
    CAN_UPGRADE_USERS(0L),
    CAN_BAN_USERS(1L),
    CAN_MANAGE_ROOMS(100L),
    CAN_MANAGE_MEMBERSHIP_CARDS(101L),
    EARLY_BOOK(10000L)
    ;

    //We use an external value instead of .ordinal() so we can rearrange and move
    // the code freely, without destroying existing permissions
    @Getter
    private final long value;

    private static final Map<Long, Permission> PERMISSIONS = new HashMap<Long, Permission>();

    @Nullable
    public static Permission get(final long value) {
        return PERMISSIONS.get(value);
    }
    static {
        for (final Permission permission : Permission.values()) {
            long permissionValue = permission.value;
            if (PERMISSIONS.containsKey(permissionValue)) {
                throw new RuntimeException("Duplicate permission id: " + permissionValue);
            }
            PERMISSIONS.put(permissionValue, permission);
        }
    }
}
