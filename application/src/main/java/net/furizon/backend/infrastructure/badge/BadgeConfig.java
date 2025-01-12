package net.furizon.backend.infrastructure.badge;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("badge")
public class BadgeConfig {
    private final String storagePath;
    private final Integer jpegQualityThreshold;
}
