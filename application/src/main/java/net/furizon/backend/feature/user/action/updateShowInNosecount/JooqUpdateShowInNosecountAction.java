package net.furizon.backend.feature.user.action.updateShowInNosecount;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqUpdateShowInNosecountAction implements UpdateShowInNosecountAction {
    @NotNull
    private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long userId, boolean showInNosecount) {
        return sqlCommand.execute(
                PostgresDSL.update(USERS)
                .set(USERS.SHOW_IN_NOSECOUNT, showInNosecount)
                .where(USERS.USER_ID.eq(userId))
        ) > 0;
    }
}
