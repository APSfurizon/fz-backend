package net.furizon.backend.infrastructure.membership;

import java.time.LocalDate;

public class MembershipYearUtils {

    public static final int MEMBERSHIP_YEAR_RESET_MONTH = 10;
    public static final int MEMBERSHIP_YEAR_RESET_DAY = 1;

    public static short getCurrentMembershipYear() {
        return getMembershipYear(LocalDate.now());
    }

    public static short getMembershipYear(LocalDate date) {
        LocalDate reset = date.withMonth(MEMBERSHIP_YEAR_RESET_MONTH).withDayOfMonth(MEMBERSHIP_YEAR_RESET_DAY);
        short year = (short) date.getYear();

        if (date.isBefore(reset)) {
            year--;
        }
        return year;
    }
}
