package net.furizon.backend.feature.gallery.action.uploads.updateSelectedUpload;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOADS;

@Component
@RequiredArgsConstructor
public class JooqUpdateSelectedUpload implements UpdateSelectedUploadAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public void invoke(long uploadId, boolean isSelected) {
        if (isSelected) {
            command.execute(
                PostgresDSL.update(UPLOADS)
                .set(UPLOADS.IS_SELECTED, UPLOADS.ID.eq(uploadId))
                .where(UPLOADS.EVENT_ID.eq(
                        PostgresDSL.select(UPLOADS.EVENT_ID)
                        .from(UPLOADS)
                        .where(UPLOADS.ID.eq(uploadId))
                ))
            );
        } else {
            command.execute(
                PostgresDSL.update(UPLOADS)
                .set(UPLOADS.IS_SELECTED, PostgresDSL.falseCondition())
                .where(UPLOADS.ID.eq(uploadId))
            );
        }
    }
}
