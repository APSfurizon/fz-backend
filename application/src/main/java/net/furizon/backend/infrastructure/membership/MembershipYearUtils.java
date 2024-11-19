package net.furizon.backend.infrastructure.membership;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.configuration.MembershipCardConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
@RequiredArgsConstructor
public class MembershipYearUtils {
    @NotNull private final MembershipCardConfig config;

    public short getCurrentMembershipYear() {
        return getMembershipYear(LocalDate.now());
    }

    public short getMembershipYear(LocalDate date) {
        int year = date.getYear();
        LocalDate reset = LocalDate.of(
                year,
                config.getCardEnumerationResetMonth(),
                config.getCardEnumerationResetDay()
        );

        if (date.isBefore(reset)) {
            year--;
        }
        return (short) year;
    }
}
