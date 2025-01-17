package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import static net.furizon.jooq.generated.Tables.MEDIA;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMediaActionImpl implements DeleteMediaAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean deleteFromDb(@NotNull List<Long> ids) {
        return sqlCommand.execute(
            PostgresDSL.deleteFrom(MEDIA)
            .where(MEDIA.MEDIA_ID.in(ids))
        ) == ids.size();
    }
}
