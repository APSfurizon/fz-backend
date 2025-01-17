package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("storage")
public class StorageConfig {
    @NotNull private final String basePath;
    @NotNull private final String mediaPath;
    @NotNull private final String fullMediaPath;
    @NotNull private final String basePublicPath;
}
