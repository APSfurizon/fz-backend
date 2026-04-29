package net.furizon.backend.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfiguration {
    @NotNull
    private final S3Config s3Config;

    private StaticCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        s3Config.getAccessKey(),
                        s3Config.getSecretKey()
                )
        );
    }

    @Bean
    @NotNull
    public S3Client customS3Client() {
        var b = S3Client.builder()
                .region(Region.of(s3Config.getRegion()))
                .forcePathStyle(s3Config.isBucketPathStyle())
                .credentialsProvider(getCredentialsProvider());

        String endpoint = s3Config.getEndpoint();
        if (endpoint != null && !endpoint.isEmpty()) {
            b.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        return b.build();
    }

    @Bean
    @NotNull
    public S3Presigner s3Presigner(@NotNull S3Client s3Client) {
        var b = S3Presigner.builder()
                .region(Region.of(s3Config.getRegion()))
                .serviceConfiguration(
                    S3Configuration.builder()
                        .pathStyleAccessEnabled(s3Config.isBucketPathStyle())
                        .build()
                )
                .s3Client(s3Client)
                .credentialsProvider(getCredentialsProvider());

        String endpoint = s3Config.getEndpoint();
        if (endpoint != null && !endpoint.isEmpty()) {
            b.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        return b.build();
    }
}
