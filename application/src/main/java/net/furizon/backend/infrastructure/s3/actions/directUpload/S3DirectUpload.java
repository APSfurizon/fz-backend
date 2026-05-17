package net.furizon.backend.infrastructure.s3.actions.directUpload;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface S3DirectUpload {
    void upload(@NotNull String key, @NotNull Path path);
}
