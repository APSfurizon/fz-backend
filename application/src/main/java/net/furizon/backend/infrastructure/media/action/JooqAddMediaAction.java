package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.media.StoreMethod;
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
    public long invoke(@NotNull String mediaPath, @NotNull String mediaType, @NotNull StoreMethod storeMethod) {
        return sqlCommand.executeResult(
            PostgresDSL.insertInto(
                MEDIA,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_STORE_METHOD
            )
            .values(
                mediaPath,
                mediaType,
                storeMethod.getMethodId()
            )
            .returning(MEDIA.MEDIA_ID)
        ).getFirst().get(MEDIA.MEDIA_ID);
    }
}
