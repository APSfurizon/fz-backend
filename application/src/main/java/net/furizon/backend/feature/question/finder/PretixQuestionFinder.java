package net.furizon.backend.feature.question.finder;

import net.furizon.backend.feature.question.PretixQuestion;
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
