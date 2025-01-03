package net.furizon.backend.feature.pretix.objects.question.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.question.PretixQuestion;
import net.furizon.backend.feature.pretix.objects.question.finder.PretixQuestionFinder;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReloadQuestionsUseCase implements UseCase<Event, List<PretixQuestion>> {
    @NotNull private final PretixQuestionFinder pretixQuestionFinder;

    @NotNull
    @Override
    public List<PretixQuestion> executor(@NotNull Event input) {
        final var pair = input.getOrganizerAndEventPair();

        return PretixPagingUtil.fetchAll(
            paging -> pretixQuestionFinder.getPagedQuestions(
                pair.getOrganizer(),
                pair.getEvent(),
                paging
            )
        );
    }
}
