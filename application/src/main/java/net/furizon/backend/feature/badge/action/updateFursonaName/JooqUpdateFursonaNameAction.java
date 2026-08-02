package net.furizon.backend.feature.badge.action.updateFursonaName;

import net.furizon.backend.infrastructure.logging.LogCall;
import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
@LogCall
public class JooqUpdateFursonaNameAction implements UpdateFursonaNameAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long userId, @NotNull String fursonaName) {
        return sqlCommand.execute(
                PostgresDSL.update(USERS)
                .set(USERS.USER_FURSONA_NAME, fursonaName)
                .where(USERS.USER_ID.eq(userId))
        ) > 0;
    }
}
