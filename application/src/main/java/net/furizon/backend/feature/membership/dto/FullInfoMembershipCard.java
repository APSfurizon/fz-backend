package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class FullInfoMembershipCard {
    @Nullable
    private final MembershipCard membershipCard;

    @NotNull
    private final PersonalUserInformation userInfo;

    @NotNull
    private final String email;

    @NotNull
    private final UserDisplayData user;

    @Nullable
    private final String fromOrderCode;

    @Builder.Default
    private boolean duplicate = false;
}
