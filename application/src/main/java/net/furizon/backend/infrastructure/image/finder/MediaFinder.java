package net.furizon.backend.infrastructure.image.finder;

import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record3;
import org.jooq.SelectJoinStep;

import java.util.List;
import java.util.Set;

public interface MediaFinder {
    @NotNull List<MediaData> findByIds(Set<Long> ids);

    @NotNull SelectJoinStep<Record3<Long, String, String>> selectMedia();
}
