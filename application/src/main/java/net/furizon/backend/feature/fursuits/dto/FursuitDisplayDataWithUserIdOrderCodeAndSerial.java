package net.furizon.backend.feature.fursuits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class FursuitDisplayDataWithUserIdOrderCodeAndSerial {
    @NotNull private final FursuitDisplayData fursuit;
    private final long ownerUserId;
    @Nullable
    private final String orderCode;
    private final long orderSerial;
}
