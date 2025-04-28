package net.furizon.backend.feature.admin.dto;

import lombok.Data;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayDataWithUserIdOrderCodeAndSerial;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PreviewFursuitBadgeResponse {
    @NotNull private final List<FursuitDisplayDataWithUserIdOrderCodeAndSerial> fursuitBadges;
}
