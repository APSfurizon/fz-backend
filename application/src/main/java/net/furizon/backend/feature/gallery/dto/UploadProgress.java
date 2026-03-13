package net.furizon.backend.feature.gallery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UploadProgress {
    private final long uploadReqId;
    @NotNull private final String uploadId;
    @NotNull private final String s3Key;
    @NotNull private final LocalDateTime expireTs;
    private final long size;
    private final long uploaderUserId;
}
