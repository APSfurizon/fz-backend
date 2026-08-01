package net.furizon.backend.feature.nosecount.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.nosecount.dto.responses.AdminCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.FursuitCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.NoseCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.SponsorCountResponse;

@Data
public class PermanentCounts {
    @NotNull
    private final AdminCountResponse admins;

    @NotNull
    private final NoseCountResponse bopos;

    @NotNull
    private final SponsorCountResponse sponsors;

    @NotNull
    private final FursuitCountResponse fursuits;
}
