package net.furizon.backend.feature.pretix.question.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.question.PretixQuestion;
import net.furizon.backend.feature.pretix.question.finder.PretixQuestionFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReloadQuestionsUseCase implements UseCase<Event, List<PretixQuestion>> {
    private final PretixQuestionFinder pretixQuestionFinder;

    @NotNull
    @Override
    public List<PretixQuestion> executor(@NotNull Event input) {
        final var pair = input.getOrganizerAndEventPair();

        return PretixPagingUtil.combineAll(
            paging -> pretixQuestionFinder.getPagedQuestions(
                pair.getFirst(),
                pair.getSecond(),
                paging
            )
        );
    }
}
