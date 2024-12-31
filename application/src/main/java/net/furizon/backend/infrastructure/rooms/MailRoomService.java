package net.furizon.backend.infrastructure.rooms;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Setter
@RequiredArgsConstructor
public class MailRoomService {
    @NotNull private final EmailSender emailSender;
    @NotNull private final UserFinder userFinder;


    public void sendProblem(long userId, @NotNull String title, @NotNull String mailBody) {
        send(userId, RoomEmailTexts.SUBJECT_ROOM_PROBLEM, title, mailBody);
    }

    private void send(long userId, @NotNull String subject, @NotNull String title, @NotNull String mailBody) {
        UserEmailData data = userFinder.getMailDataForUser(userId);
        if (data == null) {
            log.error("Could not find mail data for user {}", userId);
            return;
        }

        String mail = data.getEmail();
        log.info("Sending email to user {} ({}) with subject '{}' and title '{}'", userId, mail, subject, title);
        emailSender.fireAndForget(
                MailRequest.builder()
                        .to(mail)
                        .subject(subject)
                        .templateMessage(
                                TemplateMessage.of("old_template.jte")
                                        .addParam("fursonaName", data.getFursonaName())
                                        .addParam("title", title)
                                        .addParam("body", mailBody)
                        )
                        .build()
        );
    }
}
