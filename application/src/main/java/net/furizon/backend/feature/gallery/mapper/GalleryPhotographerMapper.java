package net.furizon.backend.feature.gallery.mapper;

import net.furizon.backend.feature.gallery.dto.GalleryPhotographer;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Table;

public class GalleryPhotographerMapper {
    public static GalleryPhotographer map(
            @NotNull Record r,
            @NotNull Field<Integer> countField,
            @NotNull Table<Record2<Long, Integer>> countTable,
            @NotNull Field<Boolean> officialPhotographer
    ) {
        return new GalleryPhotographer(
                JooqUserDisplayMapper.map(r, false, null),
                r.get(officialPhotographer),
                r.get(countTable.field(countField))
        );
    }
}
