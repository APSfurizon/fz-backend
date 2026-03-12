package net.furizon.backend.infrastructure.s3.actions.presignedUpload;

import net.furizon.backend.infrastructure.s3.dto.MultipartCreationResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface S3PresignedUpload {
    @NotNull MultipartCreationResponse startMultipart(@NotNull String fileName, long size);

    @NotNull String completeMultipart(@NotNull String uploadId, @NotNull String fileName, @NotNull List<String> etags);

    @NotNull List<Integer> listParts(@NotNull String uploadId, @NotNull String fileName);
}
