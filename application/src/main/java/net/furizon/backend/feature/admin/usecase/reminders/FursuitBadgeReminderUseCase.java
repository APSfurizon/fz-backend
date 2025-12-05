package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.TEMPLATE_FURSUIT_BADGE_UPLOAD;

@Slf4j
@Component
@RequiredArgsConstructor
public class FursuitBadgeReminderUseCase implements UseCase<Event, Integer> {
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final FursuitReminder fursuitReminder;


    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        log.info("Sending fursuit badge upload reminder emails");

        final String deadlineStr;
        OffsetDateTime deadline = badgeConfig.getEditingDeadline();
        if (deadline != null) {
            deadlineStr = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            deadlineStr = "event starts";
        }

        return fursuitReminder.sendReminders(
                fursuitFinder.getFursuitsWithoutPropic(event),
                "badge upload",
                "mail.reminder_fursuit_badge_upload.title",
                TEMPLATE_FURSUIT_BADGE_UPLOAD,
                MailVarPair.of(EmailVars.DEADLINE, deadlineStr)
        );
    }
}
