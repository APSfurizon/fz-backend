package net.furizon.backend.feature.admin.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateBadgeRequest {
    @Pattern(regexp = "^([A-Za-z0-9]{5,},?)+$")
    private @Nullable String orderCodes;

    @Pattern(regexp = "^(\\d+(-\\d+)?,?)+$")
    private @Nullable String orderSerials;
    @Pattern(regexp = "^(\\d+(-\\d+)?,?)+$")
    private @Nullable String userIds;
}
