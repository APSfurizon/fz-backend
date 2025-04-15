package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class AdminCountRole {
    @NotNull private final String roleName;
    @NotNull private final List<UserDisplayData> members;
}
