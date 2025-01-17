package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("badge")
public class BadgeConfig {
    @NotNull private final String storagePath;
    @NotNull private final String fullStoragePath;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxSizeBytes;
}
