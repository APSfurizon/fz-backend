package net.furizon.backend.feature.gallery.action.uploads.setUploadType;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.generated.enums.UploadType;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static net.furizon.jooq.generated.Tables.UPLOADS;

@Component
@RequiredArgsConstructor
public class JooqSetUploadType implements SetUploadTypeAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long uploadId, @NotNull UploadType type) {
        return command.execute(
                PostgresDSL.update(UPLOADS)
                .set(UPLOADS.UPLOAD_TYPE, type)
                .where(UPLOADS.ID.eq(uploadId))
        ) > 0;
    }

    @Override
    public boolean invoke(Collection<Long> uploadIds, @NotNull UploadType type) {
        return command.execute(
                PostgresDSL.update(UPLOADS)
                .set(UPLOADS.UPLOAD_TYPE, type)
                .where(UPLOADS.ID.in(uploadIds))
        ) == uploadIds.size();
    }
}
