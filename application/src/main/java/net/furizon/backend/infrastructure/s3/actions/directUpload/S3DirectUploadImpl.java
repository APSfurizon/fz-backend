package net.furizon.backend.infrastructure.s3.actions.directUpload;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.s3.S3Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class S3DirectUploadImpl implements S3DirectUpload {
    @NotNull
    private final S3Client s3;

    @NotNull
    private final S3Config s3Config;

    private PutObjectRequest getReq(@NotNull String key) {
        return PutObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(key)
                .build();
    }

    @Override
    public void upload(@NotNull String key, @NotNull Path path) {
        s3.putObject(getReq(key), path);
    }
}
