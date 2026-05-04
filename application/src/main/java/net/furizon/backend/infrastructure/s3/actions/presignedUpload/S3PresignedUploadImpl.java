package net.furizon.backend.infrastructure.s3.actions.presignedUpload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.s3.S3Config;
import net.furizon.backend.infrastructure.s3.dto.MultipartCreationResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.paginators.ListPartsIterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
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

    @Override
    public @NotNull MultipartCreationResponse startMultipart(@NotNull String fileName, long size) {
        final String bucket = s3Config.getBucket();
        final long partSize = s3Config.getMultipartSize();
        final long presignExpire = s3Config.getPresignExpirationMins();

        CreateMultipartUploadResponse createResponse = s3.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
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
                    .contentLength(contentLength)
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

        log.info("Starting multipath upload for file {}. File size: {}, partsNo: {}, uploadId {}",
                fileName, size, partNo - 1, uploadId);

        return MultipartCreationResponse.builder()
                .uploadKey(fileName)
                .uploadId(uploadId)
                .expiration(LocalDateTime.now().plusMinutes(presignExpire))
                .presignedUrls(presignedUrls)
                .chunkSize(partSize)
                .fileSize(size)
            .build();
    }

    @NotNull
    @Override
    public String completeMultipart(@NotNull String uploadId, @NotNull String fileName, @NotNull List<String> etags) {
        log.info("Completing multipart upload. UploadId: {}, key {}, partsNo: {}", uploadId, fileName, etags.size());
        List<CompletedPart> completedParts = new ArrayList<>(etags.size());
        int i = 1;
        for (String etag : etags) {
            completedParts.add(
                CompletedPart.builder()
                        .partNumber(i)
                        .eTag(etag)
                    .build()
            );
            i++;
        }

        CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileName)
                .uploadId(uploadId)
                .multipartUpload(completedUpload)
                .build();

        CompleteMultipartUploadResponse response = s3.completeMultipartUpload(completeRequest);
        log.debug(response.toString());
        String etag = response.eTag();
        return response.eTag().substring(etag.charAt(0) == '"' ? 1 : 0, etag.indexOf('-'));
    }

    @Override
    public void abortUpload(@NotNull String uploadId, @NotNull String fileName) {
        log.info("Aborting multipart upload. UploadId: {}, key: {}", uploadId, fileName);
        AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileName)
                .uploadId(uploadId)
                .build();

        s3.abortMultipartUpload(abortRequest);
    }

    @Override
    public @NotNull List<Integer> listParts(@NotNull String uploadId, @NotNull String fileName) {
        ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileName)
                .uploadId(uploadId)
                .build();

        ListPartsIterable paginatedResponses = s3.listPartsPaginator(listPartsRequest);
        List<Integer> uploadedParts = new ArrayList<>();
        paginatedResponses.parts().forEach(paginatedPart -> uploadedParts.add(paginatedPart.partNumber()));
        log.info("Listing parts for uploadId: {}, key: {}. Uploaded so far: {}",
                uploadId, fileName, uploadedParts.size());
        return uploadedParts;
    }
}
