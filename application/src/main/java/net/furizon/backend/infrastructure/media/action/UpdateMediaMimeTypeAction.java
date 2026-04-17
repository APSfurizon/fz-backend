package net.furizon.backend.infrastructure.media.action;

import org.jetbrains.annotations.NotNull;

public interface UpdateMediaMimeTypeAction {
    boolean invoke(long mediaId, @NotNull String mimeType);
}
