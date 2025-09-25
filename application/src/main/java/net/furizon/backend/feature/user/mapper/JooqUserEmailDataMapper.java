package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.dto.UserEmailData;
import org.jooq.Record;

import java.util.Locale;

import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;

public class JooqUserEmailDataMapper {
    public static UserEmailData map(Record record) {
        final String[] language = record.get(USERS.USER_LANGUAGE).split("_");
        return new UserEmailData(
                record.get(USERS.USER_ID),
                record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL),
                record.get(USERS.USER_FURSONA_NAME),
                Locale.of(language[0], language.length > 1 ? language[1] : language[0])
        );
    }
}
