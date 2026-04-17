package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEDIA;

@Component
@RequiredArgsConstructor
public class JooqUpdateMediaMimeTypeAction implements UpdateMediaMimeTypeAction {
    @NotNull
    private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long mediaId, @NotNull String mimeType) {
        return sqlCommand.execute(
                PostgresDSL.update(MEDIA)
                .set(MEDIA.MEDIA_TYPE, mimeType)
                .where(MEDIA.MEDIA_ID.eq(mediaId))
        ) > 0;
    }
}
