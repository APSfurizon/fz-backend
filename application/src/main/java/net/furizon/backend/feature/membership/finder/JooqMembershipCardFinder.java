package net.furizon.backend.feature.membership.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.mapper.FullInfoMembershipMapper;
import net.furizon.backend.feature.membership.mapper.MembershipCardMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;
import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.tables.MembershipInfo.MEMBERSHIP_INFO;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqMembershipCardFinder implements MembershipCardFinder {
    private final MembershipYearUtils membershipYearUtils;
    private final SqlQuery sqlQuery;

    @Override
    public int countCardsPerUserPerEvent(long userId, @NotNull Event event) {
        OffsetDateTime from = event.getDateFrom();
        if (from == null) {
            log.error("From date was unavailable for event {}. Falling back to Date.now()", event.getSlug());
            from = OffsetDateTime.now();
        }

        LocalDate date = from.toLocalDate();
        short year = membershipYearUtils.getMembershipYear(date);

        return sqlQuery.count(
                PostgresDSL
                        .select(MEMBERSHIP_CARDS.CARD_DB_ID)
                        .from(MEMBERSHIP_CARDS)
                        .where(
                                MEMBERSHIP_CARDS.USER_ID.eq(userId)
                                    .and(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year))
                        )
        );
    }

    @Nullable
    @Override
    public MembershipCard getMembershipCardByOrderId(long orderId) {
        return sqlQuery.fetchFirst(
                PostgresDSL
                        .select(
                                MEMBERSHIP_CARDS.USER_ID,
                                MEMBERSHIP_CARDS.CARD_DB_ID,
                                MEMBERSHIP_CARDS.ID_IN_YEAR,
                                MEMBERSHIP_CARDS.ISSUE_YEAR,
                                MEMBERSHIP_CARDS.ALREADY_REGISTERED,
                                MEMBERSHIP_CARDS.CREATED_FOR_ORDER
                        )
                        .from(MEMBERSHIP_CARDS)
                        .where(
                                MEMBERSHIP_CARDS.CREATED_FOR_ORDER.eq(orderId)
                        )
        ).mapOrNull(MembershipCardMapper::map);
    }

    @NotNull
    @Override
    public List<FullInfoMembershipCard> getMembershipCards(short year) {
        return sqlQuery.fetch(
            PostgresDSL
                .select(
                        MEMBERSHIP_CARDS.USER_ID,
                        MEMBERSHIP_CARDS.CARD_DB_ID,
                        MEMBERSHIP_CARDS.ID_IN_YEAR,
                        MEMBERSHIP_CARDS.ISSUE_YEAR,
                        MEMBERSHIP_CARDS.ALREADY_REGISTERED,
                        MEMBERSHIP_CARDS.CREATED_FOR_ORDER,
                        MEMBERSHIP_INFO.ID,
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
                        MEMBERSHIP_INFO.USER_ID,
                        USERS.USER_ID,
                        USERS.USER_FURSONA_NAME,
                        USERS.USER_LOCALE,
                        USERS.MEDIA_ID_PROPIC
                )
                .from(MEMBERSHIP_CARDS)
                .innerJoin(USERS)
                .on(
                    MEMBERSHIP_CARDS.USER_ID.eq(USERS.USER_ID)
                    .and(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year))
                )
                .innerJoin(MEMBERSHIP_INFO)
                .on(USERS.USER_ID.eq(MEMBERSHIP_INFO.USER_ID))
        ).stream().map(r -> r.map(FullInfoMembershipMapper::map)).toList();
    }
}
