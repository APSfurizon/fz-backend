package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.TEMPLATE_FURSUIT_BADGE_UPLOAD;

@Slf4j
@Component
@RequiredArgsConstructor
public class FursuitBringToCurrentEventReminderUseCase implements UseCase<Event, Integer> {
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final FursuitReminder fursuitReminder;

    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        log.info("Sending fursuit bring to current event reminder emails");
        return fursuitReminder.sendReminders(
                fursuitFinder.getFursuitsWithoutPropic(event),
                "bring to current event",
                "mail.reminder_fursuit_badge_upload.title",
                TEMPLATE_FURSUIT_BADGE_UPLOAD
        );
    }
}
