package net.furizon.backend.infrastructure.media.action;

import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.NotNull;

public interface PhysicallyDeleteMediaAction {
    boolean invoke(@NotNull final MediaData media, boolean deleteFromDb);
}
