package net.furizon.backend.feature.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpireAuthenticationCronjob {
    @NotNull
    private final SessionAuthenticationManager sessionAuthenticationManager;


    @Scheduled(cron = "${security.expire-sessions-cronjob}")
    public void runDelete() {
        int expired = sessionAuthenticationManager.clearExpiredSessions();
        if (expired > 0) {
            log.info("Deleted {} expired sessions", expired);
        }
    }
}
