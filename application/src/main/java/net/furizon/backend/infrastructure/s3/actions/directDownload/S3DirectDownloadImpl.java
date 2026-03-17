package net.furizon.backend.infrastructure.s3.actions.directDownload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.s3.S3Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3DirectDownloadImpl implements S3DirectDownload {
    @NotNull
    private final S3Client s3;

    @NotNull
    private final S3Config s3Config;

    private Object download(@NotNull String key,
                            @NotNull ResponseTransformer<GetObjectResponse, ?> responseTransformer)
            throws NoSuchKeyException {
        log.info("Getting object for key {}", key);
        return s3.getObject(request ->
            request
                .bucket(s3Config.getBucket())
                .key(key),
            responseTransformer
        );
    }

    @Override
    public void toFile(@NotNull String key, @NotNull Path file) throws NoSuchKeyException, IOException {
        toFile(key, file, false);
    }
    @Override
    public void toFile(@NotNull String key, @NotNull Path file, boolean replaceExisting) throws NoSuchKeyException, IOException {
        if (replaceExisting) {
            try (InputStream stream = toInputStream(key)) {
                Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw e;
            }
        } else {
            download(key, ResponseTransformer.toFile(file));
        }
    }

    @Override
    public byte[] toBytes(@NotNull String key) throws NoSuchKeyException {
        var r = download(key, ResponseTransformer.toBytes());
        return ((ResponseBytes<GetObjectResponse>) r).asByteArray();
    }

    @Override
    public InputStream toInputStream(@NotNull String key) throws NoSuchKeyException {
        var r = download(key, ResponseTransformer.toInputStream());
        return (ResponseInputStream<GetObjectResponse>) r;
    }
}
