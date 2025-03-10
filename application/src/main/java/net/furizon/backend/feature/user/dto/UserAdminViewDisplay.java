package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;

@Data
public class UserAdminViewDisplay {
    @NotNull
    private final UserDisplayData user;

    @NotNull
    private final String email;

    @NotNull
    private final PersonalUserInformation personalInfo;

    private final boolean isBanned;
}
