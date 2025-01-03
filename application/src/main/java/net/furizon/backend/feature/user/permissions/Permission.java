package net.furizon.backend.feature.user.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum Permission {
    EARLY_BOOK(0L)
    ;

    @Getter
    private long value;

    private static final Map<Long, Permission> PERMISSIONS = new HashMap<Long, Permission>();

    @Nullable
    public static Permission get(final long value) {
        return PERMISSIONS.get(value);
    }
    static {
        for (final Permission permission : Permission.values()) {
            PERMISSIONS.put(permission.value, permission);
        }
    }
}
