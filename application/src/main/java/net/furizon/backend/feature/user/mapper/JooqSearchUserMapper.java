package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqSearchUserMapper {
    @NotNull
    public static SearchUsersResponse.SearchUser map(Record record) {
        return new SearchUsersResponse.SearchUser(
                record.get(USERS.USER_ID),
                record.get(USERS.USER_FURSONA_NAME),
                record.get(MEDIA.MEDIA_PATH)
        );
    }
}
