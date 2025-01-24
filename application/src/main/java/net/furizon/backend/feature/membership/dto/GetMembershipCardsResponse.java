package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithPersonalInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor
public class GetMembershipCardsResponse {
    @NotNull
    private List<FullInfoMembershipCard> cards;

    @Nullable
    private List<UserDisplayDataWithPersonalInfo> usersAtCurrentEventWithoutCard;

    private boolean canAddCards;
}
