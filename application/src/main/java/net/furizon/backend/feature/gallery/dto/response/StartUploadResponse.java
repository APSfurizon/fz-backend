package net.furizon.backend.feature.gallery.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import net.furizon.backend.infrastructure.s3.dto.MultipartCreationResponse;

@Builder
public class StartUploadResponse {
    private final long uploadReqId;
    @NotNull private final String s3Endpoint;
    @NotNull private final String s3Bucket;
    @NotNull private final MultipartCreationResponse multipartCreationResponse;
}
