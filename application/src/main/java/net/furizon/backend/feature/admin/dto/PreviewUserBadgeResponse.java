package net.furizon.backend.feature.admin.dto;

import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithOrderCodeAndSerial;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PreviewUserBadgeResponse {
    @NotNull private final List<UserDisplayDataWithOrderCodeAndSerial> userBadges;
}
