package net.furizon.backend.feature.badge.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class BadgePrint {
    @NotNull
    private String nickname;

    @NotNull
    private String imageUrl;

    @Nullable
    private String staffRole;

    @NotNull
    private List<String> locales;
}
