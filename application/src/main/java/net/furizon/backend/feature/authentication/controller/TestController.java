package net.furizon.backend.feature.authentication.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_EMAIL_CONFIRM;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_TOO_MANY_LOGIN_ATTEMPTS;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_EMAIL_CONFIRM;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/email")
public class TestController {
    private final EmailSender emailSender;

    @PostMapping("/send")
    public void sendEmail() {
        emailSender.fireAndForgetMany(
            MailRequest.builder()
                .to(List.of("starkthedragon@outlook.com", "stanislav.migunov@outlook.com"))
                .subject(SUBJECT_EMAIL_CONFIRM)
                .templateMessage(
                    TemplateMessage.of(TEMPLATE_EMAIL_CONFIRM)
                        .addParam("link", "https://google.com")
                )
                .build(),
            MailRequest.builder()
                .to(List.of("starkthedragon@outlook.com", "stanislav.migunov@outlook.com"))
                .subject(SUBJECT_TOO_MANY_LOGIN_ATTEMPTS)
                .templateMessage(TemplateMessage.of(TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS))
                .build()
        );
    }
}
