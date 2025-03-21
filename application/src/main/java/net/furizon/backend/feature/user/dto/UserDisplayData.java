package net.furizon.backend.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
@AllArgsConstructor
public class UserDisplayData {
    private final long userId;

    @NotNull
    private final String fursonaName;

    @Nullable
    private final String locale;

    @Nullable
    private final MediaResponse propic;

    @Nullable
    private final Sponsorship sponsorship;
}
