package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.SUBJECT_EXPIRED_ID;
import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.TEMPLATE_EXPIRED_ID;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredIdReminderUseCase implements UseCase<Event, Integer> {
    @NotNull private final PersonalInfoFinder personalInfoFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final FrontendConfig frontendConfig;

    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        int n = 0;
        log.info("Sending expired id reminder emails");

        //Obtain expired ids
        Map<Long, LocalDate> expiredIdList = personalInfoFinder.findAllUserIdsByExpiredId();

        //Obtain list of emails
        List<UserEmailData> userEmails = userFinder.getMailDataForUsers(expiredIdList.keySet().stream().toList());
        MailRequest[] mails = new MailRequest[userEmails.size()];
        String userPageUrl = frontendConfig.getUserPageUrl();
        var now = LocalDate.now();
        for (UserEmailData usr : userEmails) {
            log.debug("Sending expired id reminder email to {}", usr.getEmail());
            String date = expiredIdList.getOrDefault(usr.getUserId(), now).format(ISO_LOCAL_DATE);
            mails[n] = new MailRequest(
                    usr,
                    TEMPLATE_EXPIRED_ID,
                    MailVarPair.of(EmailVars.LINK, userPageUrl),
                    MailVarPair.of(EmailVars.DEADLINE, date)
            );
            mails[n].subject(SUBJECT_EXPIRED_ID);
            n++;
        }
        log.info("Firing expired id reminder emails");
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
