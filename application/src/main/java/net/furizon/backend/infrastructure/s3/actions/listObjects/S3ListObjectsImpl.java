package net.furizon.backend.infrastructure.s3.actions.listObjects;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.s3.S3Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class S3ListObjectsImpl implements S3ListObjects {
    @NotNull
    private final S3Client s3;
    @NotNull
    private final S3Config s3Config;

    @Override
    public void forEach(Consumer<S3Object> consumer) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(s3Config.getBucket())
                .maxKeys(500)
                .build();
        ListObjectsV2Iterable listObjectsV2Iterable = s3.listObjectsV2Paginator(listObjectsV2Request);
        listObjectsV2Iterable.forEach(page -> page.contents().forEach(consumer));
    }
}
