package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;

@Data
@Builder
public class FullInfoMembershipCard {
    @NotNull
    private final MembershipCard membershipCard;

    @NotNull
    private final PersonalUserInformation userInfo;

    @NotNull
    private final String email;

    @NotNull
    private final UserDisplayData user;
}
