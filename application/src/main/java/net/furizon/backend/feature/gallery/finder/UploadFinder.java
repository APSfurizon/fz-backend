package net.furizon.backend.feature.gallery.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UploadFinder {
    int countUserUploadsOnEvent(long userId, @NotNull Event event);

    @Nullable Long getUploaderUserId(long uploadId);

    @Nullable Long getOriginalUploaderUserId(long uploadId);

    @Nullable Long getMainMediaIdFromUploadId(long uploadId);
}
