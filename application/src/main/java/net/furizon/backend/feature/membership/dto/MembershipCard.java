package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.Min;
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
}
