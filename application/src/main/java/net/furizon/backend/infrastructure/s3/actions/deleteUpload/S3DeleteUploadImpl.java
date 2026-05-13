package net.furizon.backend.infrastructure.s3.actions.deleteUpload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.s3.S3Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3DeleteUploadImpl implements S3DeleteUpload {
    @NotNull
    private final S3Client s3;

    @NotNull
    private final S3Config s3Config;

    @Override
    public void delete(@NotNull String key) {
        log.info("Delete key: {}", key);
        s3.deleteObject(
            DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucket())
                    .key(key)
                .build()
        );
    }

}
