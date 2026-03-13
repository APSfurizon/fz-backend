package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("gallery")
public class GalleryConfig {
    private final int maxLimitedUploadsPerEvent;
    private final long maxLimitedUploadSize;
    private final long maxLimitedBigUploadSize;
}
