package net.furizon.backend.infrastructure.email;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/internal/email")
@RequiredArgsConstructor
public class Controller {
    private final EmailSender emailSender;

    @GetMapping("/send")
    void send() {
        emailSender.fireAndForget(
            MailRequest.builder()
                .to("starkthedragon@outlook.com")
                .subject("Hello!")
                .templateMessage(
                    TemplateMessage.of("registration_success.jte")
                        .addParam("username", "user")
                        .addParam("email", "user@fz.com")
                        .addParam("currentYear", LocalDate.now().getYear())
                )
                .build()
        );
    }
}
