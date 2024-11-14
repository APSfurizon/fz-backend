package net.furizon.backend.infrastructure.membership;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
public class MembershipYearUtils {
    @Getter
    @Value("${membership.card-enumeration-reset-month}")
    private final int membershipYearResetMonth = 10;
    @Getter
    @Value("${membership.card-enumeration-reset-day}")
    private final int membershipYearResetDay = 1;

    public short getCurrentMembershipYear() {
        return getMembershipYear(LocalDate.now());
    }

    public short getMembershipYear(LocalDate date) {
        int year = date.getYear();
        LocalDate reset = LocalDate.of(year, membershipYearResetMonth, membershipYearResetDay);

        if (date.isBefore(reset)) {
            year--;
        }
        return (short) year;
    }
}
