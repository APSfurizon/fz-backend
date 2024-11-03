package net.furizon.backend.feature.user.action.createUser;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.furizon.jooq.generated.tables.Users.USERS;

@Component
@RequiredArgsConstructor
public class JooqCreateUserAction implements CreateUserAction {
    private final SqlCommand sqlCommand;

    @Override
    public @NotNull User invoke(@Nullable String fursonaName) {
        return sqlCommand.executeResult(
                PostgresDSL.insertInto(
                        USERS,
                        USERS.USER_FURSONA_NAME,
                        USERS.USER_SECRET
                    )
                    .values(
                        fursonaName,
                        UUID.randomUUID().toString()
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
