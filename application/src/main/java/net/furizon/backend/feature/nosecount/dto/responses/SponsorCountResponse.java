package net.furizon.backend.feature.nosecount.dto.responses;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;

import java.util.List;
import java.util.Map;

@Data
public class SponsorCountResponse {
    @NotNull private final Map<Sponsorship, List<UserDisplayData>> users;
}
