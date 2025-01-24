package net.furizon.backend.feature.user.action.createUser;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqCreateUserAction implements CreateUserAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public @NotNull User invoke(@NotNull String fursonaName, @Nullable String locale) {
        return sqlCommand.executeResult(
                PostgresDSL.insertInto(
                        USERS,
                        USERS.USER_FURSONA_NAME,
                        USERS.USER_LOCALE
                    )
                    .values(
                        fursonaName,
                        locale
                    )
                    .returning(
                        USERS.USER_ID,
                        USERS.USER_FURSONA_NAME,
                        USERS.USER_LOCALE,
                        USERS.MEDIA_ID_PROPIC,
                        USERS.SHOW_IN_NOSECOUNT
                    )
            )
            .stream()
            .map(JooqUserMapper::map)
            .findFirst()
            .orElseThrow(NullPointerException::new);
    }
}
