package net.furizon.backend.infrastructure.security.permissions.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class JooqUserHasRole {
    private final long userId;
    private final long roleId;
    @Nullable private final Long tempEventId;
}
