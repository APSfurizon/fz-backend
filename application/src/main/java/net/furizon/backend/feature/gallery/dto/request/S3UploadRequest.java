package net.furizon.backend.feature.gallery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class S3UploadRequest {
    @NotNull private long uploadReqId;
}
