package net.furizon.backend.feature.membership.action.createMembershipCard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.jooq.infrastructure.JooqOptional;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record1;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqCreateMembershipCardAction implements CreateMembershipCardAction {
    private final MembershipYearUtils membershipYearUtils;
    private final SqlCommand sqlCommand;
    private final SqlQuery sqlQuery;

    @Override
    public synchronized void invoke(long userId, @Nullable Event event) {
        OffsetDateTime from;
        if (event != null) {
            from = event.getDateFrom();
            if (from == null) {
                log.error("From date was unavailable for event {}. Falling back to Date.now()", event.getSlug());
                from = OffsetDateTime.now();
            }
        } else {
            log.error("Event object was not provided. Falling back to Date.now()");
            from = OffsetDateTime.now();
        }

        LocalDate date = from.toLocalDate();
        short year = membershipYearUtils.getMembershipYear(date);

        //We don't use sequence because we need to keep multiple active seqs at the same time
        //The choice has been between this couple of lines of code and multiple lines to
        //Obtain the correct seq object each time, checking if it existed, if not create a new one
        //and at the end of the year delete it
        JooqOptional<Record1<Integer>> r = sqlQuery.fetchFirst(
            PostgresDSL.select(
                PostgresDSL.max(MEMBERSHIP_CARDS.ID_IN_YEAR)
            ).where(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year))
        );
        int id = r.isPresent() ? r.get().get(1, Integer.class) + 1 : 1;

        sqlCommand.execute(
                PostgresDSL.insertInto(
                        MEMBERSHIP_CARDS,
                        MEMBERSHIP_CARDS.ISSUE_YEAR,
                        MEMBERSHIP_CARDS.USER_ID,
                        MEMBERSHIP_CARDS.ID_IN_YEAR
                ).values(
                        year,
                        userId,
                        id
                )
        );
    }
}
