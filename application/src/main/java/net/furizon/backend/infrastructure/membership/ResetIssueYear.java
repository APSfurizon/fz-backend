package net.furizon.backend.infrastructure.membership;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetIssueYear {

    @Value("${membership.card-enumeration-reset-month}")
    private final int resetMonth = 10;
    @Value("${membership.card-enumeration-reset-day}")
    private final int resetDay = 1;

    @Getter
    private final String cronjob = "0 0 6 " + resetDay + " " + resetMonth + " *";

    @Scheduled(cron = cronjob)
    private void resetIssueYear() {
        //TODO
        //Also: maybe a cronjob is not needed at all?
    }
}
