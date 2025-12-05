package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.TEMPLATE_FURSUIT_BRING_TO_EVENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class FursuitBringToCurrentEventReminderUseCase implements UseCase<Event, Integer> {
    @NotNull private final TranslationService translationService;
    @NotNull private final FrontendConfig frontendConfig;
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final EmailSender emailSender;

    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        int n = 0;
        log.info("Sending fursuit bring to current event reminder emails");

        List<UserEmailData> userEmails = fursuitFinder.getUsersNotBringingAnyFursuit(event);

        //Send mails
        MailRequest[] mails = new MailRequest[userEmails.size()];
        for (UserEmailData usr : userEmails) {

            log.debug("Sending fursuit bring to current event reminder email to {}", usr.getEmail());
            mails[n] = new MailRequest(
                usr,
                TEMPLATE_FURSUIT_BRING_TO_EVENT,
                MailVarPair.of(EmailVars.LINK, frontendConfig.getBadgePageUrl()),
                MailVarPair.of(EmailVars.EVENT_NAME, event.getLocalizedName(translationService))
            ).subject("mail.reminder_fursuit_bring-to-current-event.title");
            n++;
        }
        log.info("Firing fursuit bring to current event reminder emails");
        emailSender.fireAndForgetMany(mails);
        return n;
    }
}
