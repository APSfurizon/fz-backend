package net.furizon.backend.feature.gallery.action.uploads.deleteUpload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOADS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqDeleteUpload implements DeleteUploadAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long id) {
        return command.execute(
            PostgresDSL.delete(UPLOADS)
            .where(UPLOADS.ID.eq(id))
        ) > 0;
    }
}
