package net.furizon.backend.feature.pretix.objects.question;

import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import org.jetbrains.annotations.NotNull;

@Data
public class PretixQuestion {
    private final long id;

    @NotNull
    private final QuestionType type;

    @NotNull
    private final String identifier;
}
