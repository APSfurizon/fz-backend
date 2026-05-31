package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
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

import static net.furizon.backend.feature.gallery.GalleryConstant.GALLERY_POST_EVENT_REMIND_JTE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendPostEventGalleryReminderUseCase implements UseCase<Event, Integer> {

    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final FrontendConfig frontendConfig;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        int n = 0;

        final String link = frontendConfig.getGalleryPageUrl();

        List<Long> attendees = userFinder.getUsersAttendingEvent(event);
        List<UserEmailData> mailData = userFinder.getMailDataForUsers(attendees);
        MailRequest[] mails = new MailRequest[mailData.size()];
        for (UserEmailData usr : mailData) {
            log.debug("Sending post event gallery reminder email to {}", usr.getEmail());
            mails[n] = new MailRequest(
                    usr,
                    GALLERY_POST_EVENT_REMIND_JTE,
                    MailVarPair.of(EmailVars.LINK, link),
                    MailVarPair.of(EmailVars.EVENT_NAME, event.getLocalizedName(translationService, usr.getLanguage()))
            ).subject("mail.gallery_post_event_reminder.title");
        }

        log.info("Firing post event gallery reminder emails");
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
