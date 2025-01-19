package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.objects.SearchUserResult;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USERS;

public class JooqSearchUserMapper {
    @NotNull
    public static SearchUserResult map(Record record) {
        return new SearchUserResult(
                record.get(USERS.USER_ID),
                record.get(USERS.USER_FURSONA_NAME),
                MediaResponseMapper.mapOrNull(record)
        );
    }
}
