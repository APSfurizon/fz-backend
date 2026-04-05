package net.furizon.backend.feature.roles.dto.responses;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class ListedRoleResponse {
    private final long roleId;

    @NotNull private final String internalName;
    @Nullable private final String displayName;

    private final boolean showInAdminCount;

    private final long permanentUsersNumber;
    private final long temporaryUsersNumber;

    private final int permissionsNumber;
}
