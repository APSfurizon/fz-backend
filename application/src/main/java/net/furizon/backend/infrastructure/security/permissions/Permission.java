package net.furizon.backend.infrastructure.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Permission {
    CAN_UPGRADE_USERS(0L),
    CAN_BAN_USERS(1L),
    CAN_SEE_ADMIN_PAGES(2L),
    CAN_MANAGE_ROOMS(100L),
    CAN_MANAGE_MEMBERSHIP_CARDS(101L),
    CAN_REFRESH_PRETIX_CACHE(102L),
    CAN_MANAGE_RAW_UPLOADS(103L),
    EARLY_BOOK(10000L);

    // We use an external value instead of .ordinal() so we can rearrange and move
    // the code freely, without destroying existing permissions
    private final long value;

    private static final Map<Long, Permission> PERMISSIONS = Arrays
        .stream(Permission.values())
        .collect(Collectors.toMap(Permission::getValue, Function.identity()));

    @Nullable
    public static Permission get(final long value) {
        return PERMISSIONS.get(value);
    }
}
