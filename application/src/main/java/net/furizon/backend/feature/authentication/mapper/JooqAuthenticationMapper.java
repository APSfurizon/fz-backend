package net.furizon.backend.feature.authentication.mapper;

import net.furizon.backend.feature.authentication.Authentication;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Authentications.AUTHENTICATIONS;

public class JooqAuthenticationMapper {
    @NotNull
    public static Authentication map(Record record) {
        return Authentication.builder()
            .id(record.get(AUTHENTICATIONS.AUTHENTICATION_ID))
            .email(record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL))
            .isVerified(record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFIED))
            .isDisabled(record.get(AUTHENTICATIONS.AUTHENTICATION_DISABLED))
            .isTwoFactorEnabled(record.get(AUTHENTICATIONS.AUTHENTICATION_2FA_ENABLED))
            .isFrom0Auth(record.get(AUTHENTICATIONS.AUTHENTICATION_FROM_OAUTH))
            .hashedPassword(record.get(AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD))
            .userId(record.get(AUTHENTICATIONS.USER_ID))
            .build();
    }
}
