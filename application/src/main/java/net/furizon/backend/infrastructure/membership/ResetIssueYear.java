package net.furizon.backend.infrastructure.membership;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static net.furizon.backend.infrastructure.membership.MembershipYearUtils.MEMBERSHIP_YEAR_RESET_MONTH;
import static net.furizon.backend.infrastructure.membership.MembershipYearUtils.MEMBERSHIP_YEAR_RESET_DAY;

@Service
public class ResetIssueYear {

    public static final String CRONJOB =
            "0 0 6 " + MEMBERSHIP_YEAR_RESET_DAY + " " + MEMBERSHIP_YEAR_RESET_MONTH + " *";

    @Scheduled(cron = CRONJOB)
    public void resetIssueYear() {
        //TODO
        //Also: maybe a cronjob is not needed at all?
    }
}
