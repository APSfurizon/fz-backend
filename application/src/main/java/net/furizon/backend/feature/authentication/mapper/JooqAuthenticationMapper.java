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
            .mailVerificationCreationMs(record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION))
            .isDisabled(record.get(AUTHENTICATIONS.AUTHENTICATION_DISABLED))
            .hashedPassword(record.get(AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD))
            .failedAttempts(record.get(AUTHENTICATIONS.AUTHENTICATION_FAILED_ATTEMPTS))
            .authToken(record.get(AUTHENTICATIONS.AUTHENTICATION_TOKEN))
            .userId(record.get(AUTHENTICATIONS.USER_ID))
            .build();
    }
}
