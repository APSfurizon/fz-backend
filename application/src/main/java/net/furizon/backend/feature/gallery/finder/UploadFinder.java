package net.furizon.backend.feature.gallery.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UploadFinder {
    int countUserUploadsOnEvent(long userId, @NotNull Event event);

    @Nullable Long getUploaderUserId(long uploadId);

    @Nullable Long getOriginalUploaderUserId(long uploadId);

    @Nullable Long getMainMediaIdFromUploadId(long uploadId);

    @Nullable String getMainMediaFilenameFromUploadId(long uploadId);

    @Nullable Long getUploadIdByHash(@NotNull String hash);

    @Nullable Long getUploadIdByHashOnEvent(@NotNull String hash, long eventId);

    @NotNull List<Long> getUnprocessedUploadIds();
}
