package net.furizon.backend.feature.gallery.mapper;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.gallery.dto.UploadVideoMetadata;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.UPLOAD_VIDEO_DATA;

public class VideoMetadataMapper {
    @NotNull
    public static UploadVideoMetadata map(@NotNull Record r) {
        return UploadVideoMetadata.builder()
                .videoCodec(r.get(UPLOAD_VIDEO_DATA.VIDEO_CODEC))
                .audioCodec(r.get(UPLOAD_VIDEO_DATA.AUDIO_CODEC))
                .audioFrequency(r.get(UPLOAD_VIDEO_DATA.AUDIO_FREQUENCY))
                .framerate(r.get(UPLOAD_VIDEO_DATA.FRAMERATE))
                .durationMs(r.get(UPLOAD_VIDEO_DATA.DURATION))
            .build();
    }
}
