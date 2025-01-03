package net.furizon.backend.feature.user.action.createUser;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.Users.USERS;

@Component
@RequiredArgsConstructor
public class JooqCreateUserAction implements CreateUserAction {
    private final SqlCommand sqlCommand;

    @Override
    public @NotNull User invoke(@NotNull String fursonaName) {
        return sqlCommand.executeResult(
                PostgresDSL.insertInto(
                        USERS,
                        USERS.USER_FURSONA_NAME
                    )
                    .values(
                        fursonaName
                    )
                    .returning(
                        USERS.USER_ID,
                        USERS.USER_FURSONA_NAME
                    )
            )
            .stream()
            .map(JooqUserMapper::map)
            .findFirst()
            .orElseThrow(NullPointerException::new);
    }
}
