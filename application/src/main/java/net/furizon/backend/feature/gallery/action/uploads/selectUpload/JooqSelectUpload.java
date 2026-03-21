package net.furizon.backend.feature.gallery.action.uploads.selectUpload;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOADS;

@Component
@RequiredArgsConstructor
public class JooqSelectUpload implements SelectUploadAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public void invoke(long uploadId) {
        command.execute(
            PostgresDSL.update(UPLOADS)
            .set(UPLOADS.IS_SELECTED, UPLOADS.ID.eq(uploadId))
            .where(UPLOADS.EVENT_ID.eq(
                PostgresDSL.select(UPLOADS.EVENT_ID)
                .from(UPLOADS)
                .where(UPLOADS.ID.eq(uploadId))
                .limit(1)
            ))
        );
    }
}
