package net.furizon.backend.feature.fursuits.action.setBadge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqSetFursuitBadgeAction implements SetFursuitBadgeAction {
    @NotNull
    private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long fursuitId, @Nullable Long mediaId) {
        log.info("Updating fursuit badge for {} to media {}", fursuitId, mediaId);
        return sqlCommand.execute(
            PostgresDSL.update(FURSUITS)
            .set(FURSUITS.MEDIA_ID_PROPIC, mediaId)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ) > 0;
    }
}
