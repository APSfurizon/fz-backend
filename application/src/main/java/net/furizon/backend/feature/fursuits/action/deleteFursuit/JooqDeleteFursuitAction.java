package net.furizon.backend.feature.fursuits.action.deleteFursuit;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.FURSUITS;

@Component
@RequiredArgsConstructor
public class JooqDeleteFursuitAction implements DeleteFursuitAction {
    @NotNull private final SqlCommand command;

    @Override
    public boolean invoke(long fursuitId) {
        return command.execute(
            PostgresDSL.deleteFrom(FURSUITS)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ) > 0;
    }
}
