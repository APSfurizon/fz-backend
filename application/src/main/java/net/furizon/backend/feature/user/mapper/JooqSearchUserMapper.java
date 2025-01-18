package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.objects.SearchUser;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqSearchUserMapper {
    @NotNull
    public static SearchUser map(Record record) {
        return new SearchUser(
                record.get(USERS.USER_ID),
                record.get(USERS.USER_FURSONA_NAME),
                MediaResponseMapper.map(record)
        );
    }
}
