package net.furizon.backend.infrastructure.image.action;

import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface DeleteMediaFromDiskAction {
    @Transactional
    boolean invoke(@NotNull Set<Long> ids) throws IOException;

    @Transactional
    boolean invoke(long id) throws IOException;

    @Transactional
    boolean invoke(@NotNull List<MediaData> medias) throws IOException;

    @Transactional
    boolean invoke(@NotNull MediaData media) throws IOException;
}
