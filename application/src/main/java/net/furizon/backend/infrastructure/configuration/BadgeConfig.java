package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.OffsetDateTime;

@Data
@ConfigurationProperties("badge")
public class BadgeConfig {
    @NotNull private final String storagePath;
    @NotNull private final String fullStoragePath;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxSizeBytes;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Nullable private final OffsetDateTime editingDeadline;

    @NotNull private final Export export;
    @Data
    public static class Export {
        @NotNull private final String defaultImageUrl;
        @NotNull private final String outputWrapperBadgeJteFilename;
        @NotNull private final String userBadgeJteFilename;
        @NotNull private final String fursuitBadgeJteFilename;
    }
}
