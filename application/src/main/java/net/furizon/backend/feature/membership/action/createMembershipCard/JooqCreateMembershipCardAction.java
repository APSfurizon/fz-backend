package net.furizon.backend.feature.membership.action.createMembershipCard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.jooq.infrastructure.JooqOptional;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqCreateMembershipCardAction implements CreateMembershipCardAction {
    private final MembershipYearUtils membershipYearUtils;
    private final SqlCommand sqlCommand;
    private final SqlQuery sqlQuery;

    @Override
    public synchronized void invoke(long userId, @NotNull Event event, @Nullable Order order) {
        Long orderId = order == null ? null : order.getId();
        short year = event.getMembershipYear(membershipYearUtils);

        var infoRes = sqlQuery.fetchFirst(
            PostgresDSL.select(
                MEMBERSHIP_INFO.INFO_FIRST_NAME,
                MEMBERSHIP_INFO.INFO_LAST_NAME,
                MEMBERSHIP_INFO.INFO_BIRTHDAY,
                MEMBERSHIP_INFO.INFO_FISCAL_CODE
            )
            .from(MEMBERSHIP_INFO)
            .where(MEMBERSHIP_INFO.USER_ID.eq(userId))
            .limit(1)
        );
        String firstName = "";
        String lastName = "";
        LocalDate birthday = LocalDate.EPOCH;
        String fiscalCode = null;
        if (infoRes.isPresent()) {
            Record record = Objects.requireNonNull(infoRes.get());
            firstName = record.get(MEMBERSHIP_INFO.INFO_FIRST_NAME);
            lastName = record.get(MEMBERSHIP_INFO.INFO_LAST_NAME);
            birthday = record.get(MEMBERSHIP_INFO.INFO_BIRTHDAY);
            fiscalCode = record.get(MEMBERSHIP_INFO.INFO_FISCAL_CODE);
        }

        //We don't use sequence because we need to keep multiple active seqs at the same time
        //The choice has been between this couple of lines of code and multiple lines to
        //Obtain the correct seq object each time, checking if it existed, if not create a new one
        //and at the end of the year delete it
        JooqOptional<Record1<Integer>> r = sqlQuery.fetchFirst(
            PostgresDSL.select(
                PostgresDSL.max(MEMBERSHIP_CARDS.ID_IN_YEAR)
            )
            .from(MEMBERSHIP_CARDS)
            .where(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year))
        );
        int id = 1;
        if (r.isPresent()) {
            var res = Objects.requireNonNull(r.get()).get(0);
            id = res == null ? 1 : ((int) res) + 1;
        }

        sqlCommand.execute(
                PostgresDSL.insertInto(
                        MEMBERSHIP_CARDS,
                        MEMBERSHIP_CARDS.ISSUE_YEAR,
                        MEMBERSHIP_CARDS.USER_ID,
                        MEMBERSHIP_CARDS.ID_IN_YEAR,
                        MEMBERSHIP_CARDS.CREATED_FOR_ORDER,

                        MEMBERSHIP_CARDS.FIRST_NAME,
                        MEMBERSHIP_CARDS.LAST_NAME,
                        MEMBERSHIP_CARDS.BIRTHDAY,
                        MEMBERSHIP_CARDS.FISCAL_CODE
                ).values(
                        year,
                        userId,
                        id,
                        orderId,

                        firstName,
                        lastName,
                        birthday,
                        fiscalCode
                )
        );
    }
}
