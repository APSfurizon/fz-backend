package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import net.furizon.backend.infrastructure.http.client.HttpConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("gallery")
public class GalleryConfig implements HttpConfig {
    private final int maxLimitedUploadsPerEvent;
    private final long maxLimitedUploadSize;
    private final long maxLimitedBigUploadSize;

    private final long listingBatchSize;
    private final long adminApprovalBatchSize;

    @NotNull
    private final JobProcessor jobProcessor;

    @Data
    public static class JobProcessor {
        @NotNull private final String endpoint;
        @NotNull private final String username;
        @NotNull private final String password;
    }

    @Override
    public @NotNull String getBaseUrl() {
        return jobProcessor.endpoint;
    }

    @Override
    public @NotNull Pair<String, String> basicAuth() {
        return Pair.of(jobProcessor.username, jobProcessor.password);
    }
}
