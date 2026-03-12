package net.furizon.backend.infrastructure.s3.actions.presignedUpload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.s3.S3Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.CreateMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedCreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3PresignedUploadImpl implements S3PresignedUpload {

    @NotNull
    private final S3Client s3;

    @NotNull
    private final S3Presigner presigner;

    @NotNull
    private final S3Config s3Config;

    public void uploadMultipart(@NotNull String fileName, long size) {
        final String bucket = s3Config.getBucket();
        final long partSize = s3Config.getMultipartSize();
        final long presignExpire = s3Config.getPresignExpirationMins();

        CreateMultipartUploadResponse createResponse = s3.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .checksumAlgorithm("sha1")
                        .build()
        );

        final String uploadId = createResponse.uploadId();

        int partNo = 1;
        long remainingBytes = size;
        List<String> presignedUrls = new ArrayList<String>((int) ((size / partSize) + 2L)); //prealloc for performance
        while (remainingBytes > 0L) {
            long contentLength = Math.min(remainingBytes, partSize);

            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .uploadId(uploadId)
                    .contentLength(contentLength) //TODO we assume there's no min part limit for the last chunk TEST!!
                    .partNumber(partNo)
                    .build();

            UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignExpire))
                    .uploadPartRequest(uploadPartRequest)
                    .build();

            PresignedUploadPartRequest presignedRequest = presigner.presignUploadPart(presignRequest);

            presignedUrls.add(presignedRequest.url().toExternalForm());

            remainingBytes -= partSize;
            partNo++;
        }
    }
}
