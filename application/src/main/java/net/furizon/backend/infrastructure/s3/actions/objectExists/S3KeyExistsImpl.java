package net.furizon.backend.infrastructure.s3.actions.objectExists;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.s3.S3Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3KeyExistsImpl implements S3KeyExists {
    @NotNull
    private final S3Client s3;
    @NotNull
    private final S3Config s3Config;

    @Override
    public boolean invoke(@NotNull String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucket())
                    .key(key)
                    .build();

            s3.headObject(headObjectRequest);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            } else {
                throw e;
            }
        }
    }
}
