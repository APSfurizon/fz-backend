package net.furizon.backend.feature.badge.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class BadgeToPrint {

    private final long userId;

    private final long serialNo;

    private final boolean isDaily;

    @Nullable
    private final Long fursuitId;

    @NotNull
    private final String orderCode;

    @NotNull
    private final String fursonaName;

    @Nullable
    private final String fursuitName;

    @Nullable final String fursuitSpecies;

    @Nullable
    private final String imageUrl;
    @Nullable
    private final String imageMimeType;

    @NotNull
    private final List<String> locales;

    @NotNull
    private final Sponsorship sponsorship;

    private final Set<Permission> permissions;
}
