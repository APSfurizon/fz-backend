package net.furizon.backend.feature.user.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class UserDisplayDataWithOrderCodeAndSerial {
    @NotNull
    private final UserDisplayData user;
    @Nullable
    private final String orderCode;
    private final long orderSerial;
}
