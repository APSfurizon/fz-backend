package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.furizon.backend.infrastructure.admin.ReminderEmailTexts.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FursuitBadgeReminderUseCase implements UseCase<Event, Integer> {
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final BadgeConfig badgeConfig;

    @Value("${frontend.badge-page-url}")
    private String badgePageUrl;

    @Override
    public @NotNull Integer executor(@NotNull Event event) {
        int n = 0;
        log.info("Sending fursuit badge upload reminder emails");

        String deadlineStr = "event starts";
        OffsetDateTime deadline = badgeConfig.getEditingDeadline();
        if (deadline != null) {
            deadlineStr = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        //Obtain and group fursuits
        List<FursuitData> fursuits = fursuitFinder.getFursuitsWithoutPropic(event);
        Map<Long, List<FursuitData>> userToFursuits = new HashMap<>();
        for (FursuitData fursuitData : fursuits) {
            List<FursuitData> data = userToFursuits.computeIfAbsent(
                    fursuitData.getOwnerId(),
                    k -> new ArrayList<>(fursuits.size())
            );
            data.add(fursuitData);
        }

        //Obtain list of emails
        List<UserEmailData> userEmails = userFinder.getMailDataForUsers(userToFursuits.keySet().stream().toList());
        MailRequest[] mails = new MailRequest[userEmails.size()];
        for (UserEmailData usr : userEmails) {

            log.info("Sending fursuit badge upload reminder email to {}", usr.getEmail());
            String suits = String.join(
                ", ",
                userToFursuits.get(usr.getUserId()).stream().map(s -> s.getFursuit().getName()).toList()
            );
            mails[n] = new MailRequest(
                    usr,
                    TEMPLATE_FURSUIT_BADGE_UPLOAD,
                    MailVarPair.of(EmailVars.LINK, badgePageUrl),
                    MailVarPair.of(EmailVars.DEADLINE, deadlineStr),
                    MailVarPair.of(EmailVars.FURSUIT_NAME, suits)
            );
            mails[n].subject(SUBJECT_FURSUIT_BADGE_UPLOAD);
            n++;
        }
        log.info("Firing fursuit badge upload reminder emails");
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
