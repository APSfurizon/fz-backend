package net.furizon.backend.infrastructure.image.action;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEDIA;

@Component
@RequiredArgsConstructor
public class JooqAddMediaAction implements AddMediaAction {
    private final SqlCommand sqlCommand;

    @Override
    public long invoke(@NotNull String mediaPath, @NotNull String mediaType) {
        // TODO -> Better add media owner as well (userId)
        return sqlCommand
            .executeResult(
                PostgresDSL
                    .insertInto(MEDIA, MEDIA.MEDIA_PATH, MEDIA.MEDIA_TYPE).values(mediaPath, mediaType)
                    .returning(MEDIA.MEDIA_ID)
            )
            .getFirst()
            .get(MEDIA.MEDIA_ID);
    }
}
