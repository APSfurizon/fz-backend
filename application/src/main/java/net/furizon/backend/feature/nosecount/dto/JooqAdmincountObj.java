package net.furizon.backend.feature.nosecount.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class JooqAdmincountObj {
    private final long roleId;
    @NotNull private final String roleInternalName;
    @Nullable private final String roleDisplayName;

    @NotNull private final UserDisplayData user;
}
