package net.furizon.backend.infrastructure.media.action;

import org.jetbrains.annotations.NotNull;

public interface AddMediaAction {
    /**
     * @param mediaPath - should be Relative (/userId/media)
     * @param mediaType - type of media (image/x)
     * @return Created media id from database
     */
    long invoke(@NotNull String mediaPath, @NotNull String mediaType);
}
