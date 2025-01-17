package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import static net.furizon.jooq.generated.Tables.MEDIA;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMediaActionImpl implements DeleteMediaAction {
    @NotNull private final DeleteMediaFromDiskAction deleteMediaFromDiskAction;
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean deletePhysically(@NotNull final MediaData media) {
        try {
            boolean res = switch (media.getStoreMethod()) {
                case StoreMethod.DISK -> deleteMediaFromDiskAction.invoke(media); //It internally calls deleteIfExists
                default -> throw new IllegalStateException("Unexpected value: " + media.getStoreMethod());
            };
            if (!res) {
                log.error("Failed to delete media: {}", media);
            }
            return res;
        } catch (IOException e) {
            log.error("Error deleting dangling media {}. store={} path={}",
                    media.getId(), media.getStoreMethod(), media.getPath(), e);
            return false;
        }
    }

    @Override
    public boolean deleteFromDb(@NotNull List<Long> ids) {
        return sqlCommand.execute(
            PostgresDSL.deleteFrom(MEDIA)
            .where(MEDIA.MEDIA_ID.in(ids))
        ) == ids.size();
    }
}
