package net.furizon.backend.infrastructure.s3.actions.deleteUpload;

import org.jetbrains.annotations.NotNull;

public interface S3DeleteUpload {
    void delete(@NotNull String key);
}
