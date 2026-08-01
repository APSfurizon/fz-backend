package net.furizon.backend.infrastructure.nosecount;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("nosecounts")
public class NosecountConfig {
    @NotNull
    private final Permanent permanent;

    @Data
    public static class Permanent {
        @Nullable
        private final String jsonPath;
    }
}
