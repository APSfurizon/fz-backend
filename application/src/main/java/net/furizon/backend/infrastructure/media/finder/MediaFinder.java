package net.furizon.backend.infrastructure.media.finder;

import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record4;
import org.jooq.SelectJoinStep;

import java.util.List;
import java.util.Set;

public interface MediaFinder {
    @NotNull List<MediaData> findByIds(Set<Long> ids);

    @NotNull List<MediaData> findAll();

    @NotNull Set<Long> getReferencedMediaIds();

    @NotNull SelectJoinStep<Record4<Long, String, String, Integer>> selectMedia();
}
