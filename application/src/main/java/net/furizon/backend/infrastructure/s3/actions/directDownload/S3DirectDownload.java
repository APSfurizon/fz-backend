package net.furizon.backend.infrastructure.s3.actions.directDownload;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface S3DirectDownload {
    void toFile(@NotNull String key, @NotNull Path file) throws NoSuchKeyException, IOException;

    void toFile(@NotNull String key, @NotNull Path file, boolean replaceExisting) throws NoSuchKeyException, IOException;

    byte[] toBytes(@NotNull String key) throws NoSuchKeyException;
    InputStream toInputStream(@NotNull String key) throws NoSuchKeyException;
}
