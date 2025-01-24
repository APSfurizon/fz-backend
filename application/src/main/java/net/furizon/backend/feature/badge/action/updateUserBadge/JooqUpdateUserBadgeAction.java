package net.furizon.backend.feature.badge.action.updateUserBadge;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqUpdateUserBadgeAction implements UpdateUserBadgeAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(
        long userId,
        @NotNull String fursonaName,
        @NotNull String locale
    ) {
        return sqlCommand.execute(
                PostgresDSL.update(USERS)
                .set(USERS.USER_FURSONA_NAME, fursonaName)
                .set(USERS.USER_LOCALE, locale)
                .where(USERS.USER_ID.eq(userId))
        ) > 0;
    }
}
