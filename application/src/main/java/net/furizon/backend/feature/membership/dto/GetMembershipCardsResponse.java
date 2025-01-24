package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetMembershipCardsResponse {
    @NotNull
    private List<FullInfoMembershipCard> cards;

    @NotNull
    private List<FullInfoMembershipCard> usersAtCurrentEventWithoutCard;

    private boolean canAddCards;
}
