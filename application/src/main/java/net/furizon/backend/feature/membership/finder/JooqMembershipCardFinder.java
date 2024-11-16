package net.furizon.backend.feature.membership.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqMembershipCardFinder implements MembershipCardFinder {
    private final MembershipYearUtils membershipYearUtils;
    private final SqlQuery sqlQuery;

    public int countCardsPerUserPerEvent(long userId, @NotNull Event event) {
        OffsetDateTime from = event.getDateFrom();
        if (from == null) {
            log.error("From date was unavailable for event {}. Falling back to Date.now()", event.getSlug());
            from = OffsetDateTime.now();
        }

        LocalDate date = from.toLocalDate();
        short year = membershipYearUtils.getMembershipYear(date);

        //TODO: Ask stark if this is the correct way to count
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
}
