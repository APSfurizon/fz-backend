package net.furizon.backend.feature.roles.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class ListedRoleResponse {
    private final long roleId;

    @NotNull private final String roleInternalName;
    @Nullable private final String roleDisplayName;

    private final boolean showInNosecount;

    private final long permanentUsersNumber;
    private final long temporaryUsersNumber;

    private final long permissionsNumber;
}
