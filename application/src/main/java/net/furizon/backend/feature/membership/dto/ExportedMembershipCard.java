package net.furizon.backend.feature.membership.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserEmailData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Data
@Builder
public class ExportedMembershipCard {
    private final long cardDbId;
    private final int cardIdInYear;

    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
    @Nullable
    private final String fiscalCode;
    @NotNull
    private final LocalDate birthDay;

    @NotNull
    private final UserEmailData user;
}
