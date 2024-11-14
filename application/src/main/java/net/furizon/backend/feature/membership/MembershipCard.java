package net.furizon.backend.feature.membership;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@Builder
public class MembershipCard {

    private final int id = -1;

    private final int idInYear;

    private final int issueYear;

    @NotNull
    private final String email;

    @NotNull
    private final PersonalUserInformation personalUserInformation;
}
