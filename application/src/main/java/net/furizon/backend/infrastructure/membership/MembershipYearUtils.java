package net.furizon.backend.infrastructure.membership;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.configuration.MembershipConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
@RequiredArgsConstructor
public class MembershipYearUtils {
    @NotNull private final MembershipConfig config;

    public short getCurrentMembershipYear() {
        return getMembershipYear(LocalDate.now());
    }

    public @NotNull LocalDate getResetDate(int year) {
        return LocalDate.of(
                year,
                config.getCardEnumerationResetMonth(),
                config.getCardEnumerationResetDay()
        );
    }

    public short getMembershipYear(LocalDate date) {
        int year = date.getYear();
        LocalDate reset = getResetDate(year);

        if (date.isBefore(reset)) {
            year--;
        }
        return (short) year;
    }
}
