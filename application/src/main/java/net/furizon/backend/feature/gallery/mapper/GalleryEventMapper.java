package net.furizon.backend.feature.gallery.mapper;

import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.pretix.objects.event.mapper.JooqEventMapper;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Table;

public class GalleryEventMapper {
    public static @NotNull GalleryEvent map(
            @NotNull Record r,
            @NotNull Table<?> media,
            @NotNull Table<?> render,
            @NotNull Table<?> thumbnail,
            @NotNull Field<Integer> countField,
            @NotNull Table<Record2<Long, Integer>> countTable
    ) {
        MediaResponse downloadMedia = MediaResponseMapper.mapOrNull(r, media);
        MediaResponse displayMedia = MediaResponseMapper.mapOrNull(r, render);
        MediaResponse thumbnailMedia = MediaResponseMapper.mapOrNull(r, thumbnail);
        //we assume that only photos can be selected
        // This should be guaranteed by the select upload usecase
        return new GalleryEvent(
            JooqEventMapper.map(r),
            displayMedia != null ? displayMedia : downloadMedia,
            thumbnailMedia,
            r.get(countTable.field(countField))
        );
    }
}
