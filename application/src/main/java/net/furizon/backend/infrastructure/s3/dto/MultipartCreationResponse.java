package net.furizon.backend.infrastructure.s3.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MultipartCreationResponse {
    @NotNull private final String uploadKey;
    @NotNull private final String uploadId;
    @NotNull private final LocalDateTime expiration;
    @NotNull private final List<String> presignedUrls;
    private final long chunkSize;
    private final long fileSize;
}
