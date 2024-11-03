package net.furizon.backend.feature.authentication.action.createAuthentication;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;

@Component
@RequiredArgsConstructor
public class JooqCreateAuthenticationAction implements CreateAuthenticationAction {
    private final SqlCommand sqlCommand;

    private final PasswordEncoder encoder;

    @Override
    public void invoke(
        long userId,
        @NotNull String email,
        @NotNull String password
    ) {
        sqlCommand.execute(
            PostgresDSL
                .insertInto(
                    AUTHENTICATIONS,
                    AUTHENTICATIONS.USER_ID,
                    AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                    AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD
                )
                .values(
                    userId,
                    email,
                    encoder.encode(password) // TODO -> Apply secret for password encoding
                )
        );
    }
}
