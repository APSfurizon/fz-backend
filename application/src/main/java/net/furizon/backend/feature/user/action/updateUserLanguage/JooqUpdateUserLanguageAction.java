package net.furizon.backend.feature.user.action.updateUserLanguage;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqUpdateUserLanguageAction implements UpdateUserLanguageAction {
    @NotNull
    private final SqlCommand sqlCommand;

    public boolean invoke(long userId, Locale userLocale) {
        return sqlCommand.execute(
                PostgresDSL.update(USERS)
                        .set(USERS.USER_LOCALE, userLocale.toString())
                        .where(USERS.USER_ID.eq(userId))
        ) > 0;
    }

}
