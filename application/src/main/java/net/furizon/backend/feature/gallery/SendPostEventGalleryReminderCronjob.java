package net.furizon.backend.feature.gallery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.usecase.SendPostEventGalleryReminderUseCase;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendPostEventGalleryReminderCronjob {
    @NotNull
    private final PretixInformation pretixInformation;
    @NotNull
    private final UseCaseExecutor useCaseExecutor;
    @NotNull
    private final GalleryConfig galleryConfig;

    @Scheduled(cron = "0 30 10,18 * * *")
    public void run() {
        Event event = pretixInformation.getCurrentEvent();
        OffsetDateTime end = event.getCorrectDateTo();
        if (end == null) {
            return;
        }
        LocalDate endDay = end.toLocalDate();

        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = now.toLocalDate();
        long daysPassed = ChronoUnit.DAYS.between(endDay, today);
        long reminderDays = galleryConfig.getPostEventGalleryReminderDays();
        if (daysPassed == reminderDays) {
            DayOfWeek dayOfWeek = endDay.getDayOfWeek();

            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            boolean weekendTime = now.getHour() == 10;

            if (isWeekend == weekendTime) {
                log.info("Post event gallery reminder cronjob running");
                int mails = useCaseExecutor.execute(SendPostEventGalleryReminderUseCase.class, event);
                log.info("Sent {} post event gallery reminders", mails);
            }
        }
    }
}
