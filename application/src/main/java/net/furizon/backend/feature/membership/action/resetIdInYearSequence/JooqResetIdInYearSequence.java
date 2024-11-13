package net.furizon.backend.feature.membership.action.resetIdInYearSequence;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Sequences.MEMBERSHIP_CARDS_ID_IN_YEARS;

@Component
@RequiredArgsConstructor
public class JooqResetIdInYearSequence implements ResetIdInYearSequence {
    private final SqlCommand sqlCommand;

    @Override
    public void invoke() {
        sqlCommand.execute(
                PostgresDSL.alterSequence(MEMBERSHIP_CARDS_ID_IN_YEARS).restart()
        );
    }
}
