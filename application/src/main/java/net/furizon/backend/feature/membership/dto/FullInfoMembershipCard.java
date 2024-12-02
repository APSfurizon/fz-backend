package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.user.User;

@Data
@Builder
public class FullInfoMembershipCard {
    @NotNull
    private final MembershipCard membershipCard;

    @NotNull
    private final PersonalUserInformation userInfo;

    @NotNull
    private final User user;
}
