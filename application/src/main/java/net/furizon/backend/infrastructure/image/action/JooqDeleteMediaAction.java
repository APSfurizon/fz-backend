package net.furizon.backend.infrastructure.image.action;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import static net.furizon.jooq.generated.Tables.MEDIA;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JooqDeleteMediaAction implements DeleteMediaAction {
    private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(@NotNull Set<Long> ids) {
        return sqlCommand
            .execute(
                PostgresDSL
                    .deleteFrom(MEDIA)
                    .where(MEDIA.MEDIA_ID.in(ids))
            ) == ids.size();
    }
}
