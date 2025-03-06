package net.furizon.backend.feature.roles.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class ListedRoleResponse {
    private final long roleId;

    @NotNull private final String roleInternalName;
    @Nullable private final String roleDisplayName;

    private final long permanentUsersNo;
    private final long tempuraryUsersNo;

    private final long permissionsNo;
}
