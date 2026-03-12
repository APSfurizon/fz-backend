package net.furizon.backend.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfiguration {
    @NotNull
    private final S3Config s3Config;

    @Bean
    @NotNull
    public S3Client customS3Client() {
        var b = S3Client.builder()
                .region(Region.of(s3Config.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        s3Config.getAccessKey(),
                                        s3Config.getSecretKey()
                                )
                        )
                );

        String endpoint = s3Config.getEndpoint();
        if (endpoint != null && !endpoint.isEmpty()) {
            b.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        return b.build();
    }
}
