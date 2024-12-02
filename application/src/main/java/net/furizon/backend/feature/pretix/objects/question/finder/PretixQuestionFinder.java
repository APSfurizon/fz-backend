package net.furizon.backend.feature.pretix.objects.question.finder;

import net.furizon.backend.feature.pretix.objects.question.PretixQuestion;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.jetbrains.annotations.NotNull;

public interface PretixQuestionFinder {
    @NotNull
    PretixPaging<PretixQuestion> getPagedQuestions(
        @NotNull String organizer,
        @NotNull String event,
        int page
    );
}
