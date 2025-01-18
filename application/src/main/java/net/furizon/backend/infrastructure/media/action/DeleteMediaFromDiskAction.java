package net.furizon.backend.infrastructure.media.action;

import net.furizon.backend.infrastructure.media.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface DeleteMediaFromDiskAction {
    @Transactional
    boolean invoke(@NotNull Set<Long> ids, boolean deleteFromDb) throws IOException;

    @Transactional
    boolean invoke(long id, boolean deleteFromDb) throws IOException;

    @Transactional
    boolean invoke(@NotNull List<MediaData> medias, boolean deleteFromDb) throws IOException;

    @Transactional
    boolean invoke(@NotNull MediaData media, boolean deleteFromDb) throws IOException;
}
