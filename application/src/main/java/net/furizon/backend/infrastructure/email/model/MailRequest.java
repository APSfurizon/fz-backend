package net.furizon.backend.infrastructure.email.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class MailRequest {
    @NotNull
    private final List<String> to;

    @NotNull
    private final String subject;

    @Nullable
    private final String message;

    @Nullable
    private final TemplateMessage templateMessage;
}
