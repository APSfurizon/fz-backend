package net.furizon.backend.feature.user.action.setBadge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.USERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqSetUserBadgeAction implements SetUserBadgeAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long userId, @Nullable Long mediaId) {
        log.info("Updating user badge for {} to media {}", userId, mediaId);
        return sqlCommand.execute(
            PostgresDSL.update(USERS)
            .set(USERS.MEDIA_ID_PROPIC, mediaId)
            .where(USERS.USER_ID.eq(userId))
        ) > 0;
    }
}
