package net.furizon.backend.feature.fursuits.action.updateFursuit;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.FURSUITS;

@Component
@RequiredArgsConstructor
public class JooqUpdateFursuitAction implements UpdateFursuitAction {
    @NotNull private final SqlCommand command;

    @Override
    public boolean invoke(long fursuitId,
                          @NotNull String name,
                          @NotNull String species,
                          boolean showInFursuitCount,
                          boolean showOwner) {
        return command.execute(
            PostgresDSL.update(FURSUITS)
            .set(FURSUITS.FURSUIT_NAME, name)
            .set(FURSUITS.FURSUIT_SPECIES, species)
            .set(FURSUITS.SHOW_IN_FURSUITCOUNT, showInFursuitCount)
            .set(FURSUITS.SHOW_OWNER, showOwner)
            .where(FURSUITS.FURSUIT_ID.eq(fursuitId))
        ) > 0;
    }
}
