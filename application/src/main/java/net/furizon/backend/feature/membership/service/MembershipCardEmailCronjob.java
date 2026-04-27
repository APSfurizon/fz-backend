package net.furizon.backend.feature.membership.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.usecase.SendMembershipCardEmails;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipCardEmailCronjob {
    @NotNull
    private final UseCaseExecutor executor;
    @NotNull
    private final PretixInformation pretixInformation;

    @Scheduled(cron = "${membership.send-card-email-cronjob}")
    public void run() {
        log.info("Running cronjob for sending membership card emails");
        int no = executor.execute(SendMembershipCardEmails.class, pretixInformation.getCurrentEvent());
        log.info("Sent {} membership card emails", no);
    }
}
