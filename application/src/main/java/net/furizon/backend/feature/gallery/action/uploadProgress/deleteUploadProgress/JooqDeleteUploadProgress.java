package net.furizon.backend.feature.gallery.action.uploadProgress.deleteUploadProgress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOAD_PROGRESS_INFO;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqDeleteUploadProgress implements DeleteUploadProgressAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long uploadReqId) {
        log.info("Deleting upload progress with id {}", uploadReqId);
        return command.execute(
                PostgresDSL.delete(UPLOAD_PROGRESS_INFO)
                .where(UPLOAD_PROGRESS_INFO.ID.eq(uploadReqId))
        ) > 0;
    }
}
