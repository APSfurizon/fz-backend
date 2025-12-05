package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class FursuitReminder {
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final FrontendConfig frontendConfig;

    public int sendReminders(@NotNull List<FursuitData> fursuits,
                             @NotNull String logName,
                             @NotNull @PropertyKey(resourceBundle = "email.email") String mailSubject,
                             @NotNull String mailTemplate,
                             MailVarPair... extraMailVariables) {
        int n = 0;
        //Collect fursuit per user
        Map<Long, List<FursuitData>> userToFursuits = new HashMap<>();
        for (FursuitData fursuitData : fursuits) {
            List<FursuitData> data = userToFursuits.computeIfAbsent(
                    fursuitData.getOwnerId(),
                    k -> new ArrayList<>(fursuits.size())
            );
            data.add(fursuitData);
        }

        //Merge mail variables
        MailVarPair[] mailVars = new MailVarPair[extraMailVariables.length + 2];
        mailVars[0] = MailVarPair.of(EmailVars.LINK, frontendConfig.getBadgePageUrl());
        System.arraycopy(extraMailVariables, 0, mailVars, 2, extraMailVariables.length);
        //Obtain list of emails
        List<UserEmailData> userEmails = userFinder.getMailDataForUsers(userToFursuits.keySet().stream().toList());
        MailRequest[] mails = new MailRequest[userEmails.size()];
        //Send mails
        for (UserEmailData usr : userEmails) {

            log.debug("Sending fursuit {} reminder email to {}", logName, usr.getEmail());
            String suits = String.join(
                    ", ",
                    userToFursuits.get(usr.getUserId()).stream().map(s -> s.getFursuit().getName()).toList()
            );
            mailVars[1] = MailVarPair.of(EmailVars.FURSUIT_NAME, suits);
            mails[n] = new MailRequest(usr, mailTemplate, mailVars).subject(mailSubject);
            n++;
        }
        log.info("Firing fursuit {} reminder emails", logName);
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
