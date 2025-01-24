package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;

@Data
public class UserDisplayDataWithPersonalInfo {
    @NotNull
    private final UserDisplayDataWithOrderCode user;
    @NotNull
    private final String email;
    @NotNull
    private final PersonalUserInformation personalInfo;
}
