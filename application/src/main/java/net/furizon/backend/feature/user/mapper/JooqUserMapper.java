package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Locale;

import static net.furizon.jooq.generated.Tables.USERS;

public class JooqUserMapper {
    @NotNull
    public static User map(Record record) {
        final String[] language = record.get(USERS.USER_LANGUAGE).split("_");
        return User.builder()
            .id(record.get(USERS.USER_ID))
            .fursonaName(record.get(USERS.USER_FURSONA_NAME))
            .locale(record.get(USERS.USER_LOCALE))
            .propicId(record.get(USERS.MEDIA_ID_PROPIC))
            .showInNoseCount(record.get(USERS.SHOW_IN_NOSECOUNT))
            .language(Locale.of(language[0], language.length > 1 ? language[1] : language[0]))
            .build();
    }
}
