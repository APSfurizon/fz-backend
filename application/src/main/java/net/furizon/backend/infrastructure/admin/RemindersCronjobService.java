package net.furizon.backend.infrastructure.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.usecase.reminders.FursuitBadgeReminderUseCase;
import net.furizon.backend.feature.admin.usecase.reminders.OrderLinkReminderUseCase;
import net.furizon.backend.feature.admin.usecase.reminders.UserBadgeReminderUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemindersCronjobService {
    @NotNull private final UseCaseExecutor useCaseExecutor;
    @NotNull private final PretixInformation pretixInformation;

    @Scheduled(cron = "${reminders.cronjobs.order-linking}")
    public void orderLink() {
        log.info("Remind order linking cronjob is running");
        int sent = useCaseExecutor.execute(OrderLinkReminderUseCase.class, pretixInformation);
        log.info("Sent {} order linking emails", sent);
    }

    @Scheduled(cron = "${reminders.cronjobs.user-badge-upload}")
    public void userBadge() {
        log.info("Remind user badge upload cronjob is running");
        int sent = useCaseExecutor.execute(UserBadgeReminderUseCase.class, pretixInformation.getCurrentEvent());
        log.info("Sent {} user badge upload emails", sent);
    }

    @Scheduled(cron = "${reminders.cronjobs.fursuit-badge-upload}")
    public void fursuitBadge() {
        log.info("Remind fursuit badge upload cronjob is running");
        int sent = useCaseExecutor.execute(FursuitBadgeReminderUseCase.class, pretixInformation.getCurrentEvent());
        log.info("Sent {} fursuit badge upload emails", sent);
    }
}
