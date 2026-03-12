package net.furizon.backend.infrastructure.s3;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage.s3")
public class S3Config {
    @Nullable private final String endpoint;
    @NotNull private final String accessKey;
    @NotNull private final String secretKey;
    @NotNull private final String region;
    @NotNull private final String bucket;
}
