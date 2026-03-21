package net.furizon.backend.feature.gallery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class S3UploadRequest {
    @NotNull private long uploadReqId;

    @Nullable private Long userId; //For admin operations
}
