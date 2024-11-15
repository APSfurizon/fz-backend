package net.furizon.backend.feature.membership.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.mapper.MembershipInfoMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.Nullable;
import org.jooq.exception.NoDataFoundException;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.MembershipInfo.MEMBERSHIP_INFO;

@Component
@RequiredArgsConstructor
public class JooqPersonalInfoFinder implements PersonalInfoFinder {
    private final SqlQuery sqlQuery;

    @Override
    public @Nullable PersonalUserInformation findByUserId(long userId) {
        try {
            return sqlQuery
                .fetchSingle(
                    PostgresDSL
                        .select(
                            MEMBERSHIP_INFO.INFO_FIRST_NAME,
                            MEMBERSHIP_INFO.INFO_LAST_NAME,
                            MEMBERSHIP_INFO.INFO_FISCAL_CODE,
                            MEMBERSHIP_INFO.INFO_BIRTH_CITY,
                            MEMBERSHIP_INFO.INFO_BIRTH_REGION,
                            MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY,
                            MEMBERSHIP_INFO.INFO_BIRTHDAY,
                            MEMBERSHIP_INFO.INFO_ADDRESS,
                            MEMBERSHIP_INFO.INFO_ZIP,
                            MEMBERSHIP_INFO.INFO_CITY,
                            MEMBERSHIP_INFO.INFO_REGION,
                            MEMBERSHIP_INFO.INFO_COUNTRY,
                            MEMBERSHIP_INFO.INFO_PHONE,
                            MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID,
                            MEMBERSHIP_INFO.USER_ID
                        )
                        .from(MEMBERSHIP_INFO)
                        .where(MEMBERSHIP_INFO.USER_ID.eq(userId))
                )
                .map(MembershipInfoMapper::map);
        } catch (NoDataFoundException e) {
            return null;
        }
    }
}
