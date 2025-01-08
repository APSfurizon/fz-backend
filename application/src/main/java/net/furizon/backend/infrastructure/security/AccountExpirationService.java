package net.furizon.backend.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountExpirationService {
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;

    @Scheduled(cron = "${security.delete-unverified-cronjob}")
    public void runChecks() {
        log.info("Running unverified account/passwords reset deletions");
        int deletedMails = sessionAuthenticationManager.deleteUnverifiedEmailAccounts();
        log.info("Deleted {} unverified email accounts", deletedMails);
        int deletedPwReset = sessionAuthenticationManager.deleteExpiredPasswordResets();
        log.info("Deleted {} expired password resets", deletedPwReset);
    }
}
