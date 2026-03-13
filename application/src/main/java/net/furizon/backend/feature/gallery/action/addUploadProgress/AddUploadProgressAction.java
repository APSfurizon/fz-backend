package net.furizon.backend.feature.gallery.action.addUploadProgress;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public interface AddUploadProgressAction {
    long invoke(
            @NotNull String uploadId,
            @NotNull String keyName,
            @NotNull LocalDateTime expiration,
            long size,
            long userId
    );
}
