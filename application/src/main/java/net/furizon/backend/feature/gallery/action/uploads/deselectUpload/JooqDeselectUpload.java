package net.furizon.backend.feature.gallery.action.uploads.deselectUpload;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOADS;

@Component
@RequiredArgsConstructor
public class JooqDeselectUpload implements DeselectUploadAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public void invoke(long uploadId) {
        command.execute(
            PostgresDSL.update(UPLOADS)
            .set(UPLOADS.IS_SELECTED, PostgresDSL.falseCondition())
            .where(UPLOADS.ID.eq(uploadId))
        );
    }
}
