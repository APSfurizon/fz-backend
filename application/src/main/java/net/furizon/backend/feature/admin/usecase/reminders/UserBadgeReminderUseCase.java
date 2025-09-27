package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.SUBJECT_USER_BADGE_UPLOAD;
import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.TEMPLATE_USER_BADGE_UPLOAD;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBadgeReminderUseCase implements UseCase<Event, Integer> {
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final BadgeConfig badgeConfig;

    @Value("${frontend.badge-page-url}")
    private String badgePageUrl;

    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        int n = 0;
        log.info("Sending user badge upload reminder emails");

        final String deadlineStr;
        OffsetDateTime deadline = badgeConfig.getEditingDeadline();
        if (deadline != null) {
            deadlineStr = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            deadlineStr = "event starts";
        }

        List<UserEmailData> usrs = userFinder.getMailDataForUsersWithNoPropic(event);
        MailRequest[] mails = new MailRequest[usrs.size()];
        for (UserEmailData usr : usrs) {
            log.info("Sending user badge upload reminder email to {}", usr.getEmail());
            mails[n] = new MailRequest(
                    usr,
                    TEMPLATE_USER_BADGE_UPLOAD,
                    MailVarPair.of(EmailVars.LINK, badgePageUrl),
                    MailVarPair.of(EmailVars.DEADLINE, deadlineStr)
            ).subject(SUBJECT_USER_BADGE_UPLOAD);
            n++;
        }
        log.info("Firing user badge upload reminder emails");
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
