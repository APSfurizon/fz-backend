package net.furizon.backend.feature.authentication.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.authentication.mapper.JooqAuthenticationMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.exception.NoDataFoundException;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.Authentications.AUTHENTICATIONS;

@Component
@RequiredArgsConstructor
public class JooqAuthenticationFinder implements AuthenticationFinder {
    private final SqlQuery sqlQuery;

    @Override
    public @Nullable Authentication findByEmail(@NotNull String email) {
        try {
            return sqlQuery
                .fetchSingle(
                    PostgresDSL
                        .select(
                            AUTHENTICATIONS.AUTHENTICATION_ID,
                            AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                            AUTHENTICATIONS.AUTHENTICATION_EMAIL_VERIFICATION_CREATION_MS,
                            AUTHENTICATIONS.AUTHENTICATION_DISABLED,
                            AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD,
                            AUTHENTICATIONS.AUTHENTICATION_TOKEN,
                            AUTHENTICATIONS.USER_ID
                        )
                        .from(AUTHENTICATIONS)
                        .where(AUTHENTICATIONS.AUTHENTICATION_EMAIL.eq(email))
                )
                .map(JooqAuthenticationMapper::map);
        } catch (NoDataFoundException e) {
            return null;
        }
    }
}
