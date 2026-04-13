package net.furizon.backend.feature.gallery.mapper;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.gallery.dto.UploadVideoMetadata;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.UPLOAD_VIDEO_DATA;

public class VideoMetadataMapper {
    @Nullable
    public static UploadVideoMetadata map(@NotNull Record r) {
        String videoCodec = r.get(UPLOAD_VIDEO_DATA.VIDEO_CODEC);
        String audioCodec = r.get(UPLOAD_VIDEO_DATA.AUDIO_CODEC);
        String audioFrequency = r.get(UPLOAD_VIDEO_DATA.AUDIO_FREQUENCY);
        String framerate = r.get(UPLOAD_VIDEO_DATA.FRAMERATE);
        Integer duration = r.get(UPLOAD_VIDEO_DATA.DURATION);

        if (videoCodec == null
                && audioCodec == null
                && audioFrequency == null
                && framerate == null
                && duration == null) {
            return null;
        }

        return UploadVideoMetadata.builder()
                .videoCodec(videoCodec)
                .audioCodec(audioCodec)
                .audioFrequency(audioFrequency)
                .framerate(framerate)
                .durationMs(duration)
            .build();
    }
}
