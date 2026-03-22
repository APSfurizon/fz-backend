package net.furizon.backend.feature.gallery.action.uploads.upsertVideoMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.UploadVideoMetadata;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOAD_VIDEO_DATA;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpsertVideoMetadata implements UpsertVideoMetadataAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long uploadId, @NotNull UploadVideoMetadata videoMetadata) {
        return command.execute(
            PostgresDSL.insertInto(
                UPLOAD_VIDEO_DATA,
                UPLOAD_VIDEO_DATA.UPLOAD_ID,
                UPLOAD_VIDEO_DATA.VIDEO_CODEC,
                UPLOAD_VIDEO_DATA.AUDIO_CODEC,
                UPLOAD_VIDEO_DATA.AUDIO_FREQUENCY,
                UPLOAD_VIDEO_DATA.DURATION,
                UPLOAD_VIDEO_DATA.FRAMERATE
            )
            .values(
                uploadId,
                videoMetadata.getVideoCodec(),
                videoMetadata.getAudioCodec(),
                videoMetadata.getAudioFrequency(),
                videoMetadata.getDuration(),
                videoMetadata.getFramerate()
            )
            .onConflict(UPLOAD_VIDEO_DATA.UPLOAD_ID)
            .doUpdate()
            .set(UPLOAD_VIDEO_DATA.VIDEO_CODEC, videoMetadata.getVideoCodec())
            .set(UPLOAD_VIDEO_DATA.AUDIO_CODEC, videoMetadata.getAudioCodec())
            .set(UPLOAD_VIDEO_DATA.AUDIO_FREQUENCY, videoMetadata.getAudioFrequency())
            .set(UPLOAD_VIDEO_DATA.DURATION, videoMetadata.getDuration())
            .set(UPLOAD_VIDEO_DATA.FRAMERATE, videoMetadata.getFramerate())
        ) > 0;
    }
}
