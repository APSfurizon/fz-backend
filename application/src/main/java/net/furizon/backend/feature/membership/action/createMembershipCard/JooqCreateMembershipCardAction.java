package net.furizon.backend.feature.membership.action.createMembershipCard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
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

    @Override
    public void invoke(long userId, @NotNull Event event) {
        OffsetDateTime from = event.getDateFrom();
        if (from == null) {
            log.error("From date was unavailable for event {}. Falling back to Date.now()", event.getSlug());
            from = OffsetDateTime.now();
        }

        LocalDate date = from.toLocalDate();
        short year = membershipYearUtils.getMembershipYear(date);

        sqlCommand.execute(
                PostgresDSL.insertInto(
                        MEMBERSHIP_CARDS,
                        MEMBERSHIP_CARDS.ISSUE_YEAR,
                        MEMBERSHIP_CARDS.USER_ID
                ).values(
                        year,
                        userId
                )
        );
    }
}
