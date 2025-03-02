package net.furizon.backend.infrastructure.media;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("image")
public class ImageConfig {
    @NotNull private final String basePath;
    @NotNull private final String fullBasePath;
    private final int webpQuality;
}
