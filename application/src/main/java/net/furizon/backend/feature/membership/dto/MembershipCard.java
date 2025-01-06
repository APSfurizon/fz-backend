package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class MembershipCard {
    @Min(0L)
    private final long cardId;
    @Min(1L)
    private final int idInYear;

    @Nullable
    @Builder.Default
    //This respects Furizon's card numbering. You may want to change the constructor with your impl
    private final String cardNo = null;

    @Min(1970L)
    private final short issueYear;

    private final long userOwnerId;

    @Nullable
    private final Long createdForOrderId;

    @Getter(AccessLevel.NONE)
    private final boolean isRegistered;
    //If the membership card has been already registered in the external italian portal
    public boolean isRegistered() {
        return isRegistered;
    }

    public MembershipCard(long cardId, int idInYear, short issueYear,
                          long userOwnerId, @Nullable Long createdForOrderId, boolean isRegistered) {
        this.cardId = cardId;
        this.idInYear = idInYear;
        this.issueYear = issueYear;
        this.userOwnerId = userOwnerId;
        this.createdForOrderId = createdForOrderId;
        this.isRegistered = isRegistered;

        int year = (int) issueYear % 100;
        cardNo = String.format("%02d%02d%03d", year, year + 1, idInYear);
    }
}
