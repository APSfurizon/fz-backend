package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.dto.UserEmailData;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;

public class JooqUserEmailDataMapper {
    public static UserEmailData map(Record record) {
        return new UserEmailData(
                record.get(USERS.USER_ID),
                record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL),
                record.get(USERS.USER_FURSONA_NAME)
        );
    }
}
