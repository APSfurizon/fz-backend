package net.furizon.backend.feature.roles.action.updateRoleInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UpdateRoleInformationAction {
    boolean invoke(
            long roleId,
            @NotNull String roleInternalName,
            @Nullable String roleDisplayName,
            long roleAdmincountPriority,
            boolean showInAdminCount
    );
}
