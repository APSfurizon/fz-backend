package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.event.Event;
import net.furizon.backend.feature.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.feature.question.PretixQuestion;
import net.furizon.backend.feature.question.usecase.ReloadQuestionsUseCase;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static net.furizon.backend.infrastructure.pretix.Const.QUESTIONS_ACCOUNT_SECRET;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedPretixInformation implements PretixInformation {
    @NotNull private final UseCaseExecutor useCaseExecutor;

    @NotNull private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);
    @NotNull private final AtomicReference<Integer> questionSecretId = new AtomicReference<>(-1);
    
    @NotNull private final Cache<Integer, QuestionType> questionIdToType = Caffeine.newBuilder().build();
    @NotNull private final Cache<Integer, String> questionIdToIdentifier = Caffeine.newBuilder().build();
    @NotNull private final Cache<String, Integer> questionIdentifierToId = Caffeine.newBuilder().build();

    @PostConstruct
    public void init() {
        log.info("[PRETIX] Initializing pretix information and cache it");
        resetCache();
    }

    @NotNull
    @Override
    public Optional<Event> getCurrentEvent() {
        return Optional.ofNullable(currentEvent.get());
    }

    @Override
    public int getQuestionSecretId() {
        return questionSecretId.get();
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeById(int id) {
        return Optional.ofNullable(questionIdToType.getIfPresent(id));
    }

    @Override
    public void resetCache() {
        log.info("[PRETIX] Resetting cache for pretix information");

        invalidateEventsCache();
        invalidateEventStructCache();

        // reloading events
        reloadEvents();
        reloadEventStructure();
    }

    private void invalidateEventsCache() {
        currentEvent.set(null);
    }

    private void invalidateEventStructCache() {
        //Questions
        questionSecretId.set(-1);
        questionIdToType.invalidateAll();
        questionIdToIdentifier.invalidateAll();
        questionIdentifierToId.invalidateAll();
    }


    private void reloadEvents() {
        useCaseExecutor
            .execute(ReloadEventsUseCase.class, UseCaseInput.EMPTY)
            .ifPresent(event -> {
                log.info("[PRETIX] Setting an event as current = '{}'", event);
                currentEvent.set(event);
            });
    }

    private void reloadEventStructure() {
        final Event event = currentEvent.get();
        if (event == null) {
            log.warn("[PRETIX] Current event is null, skipping event structure reload");
            return;
        }

        reloadQuestions(event);
    }

    private void reloadQuestions(Event event) {
        List<PretixQuestion> questionList = useCaseExecutor.execute(ReloadQuestionsUseCase.class, event);
        questionList.forEach(question -> {
            int questionId = question.getId();
            QuestionType questionType = question.getType();
            String questionIdentifier = question.getIdentifier();

            questionIdToType.put(questionId, questionType);
            questionIdToIdentifier.put(questionId, questionIdentifier);
            questionIdentifierToId.put(questionIdentifier, questionId);
        });
        // searching QUESTIONS_ACCOUNT_SECRET
        questionList.stream()
            .filter(it -> it.getIdentifier().equals(QUESTIONS_ACCOUNT_SECRET))
            .findFirst()
            .ifPresent(it -> {
                log.info("[PRETIX] Account secret id found, setup it on value = '{}'", it.getId());
                questionSecretId.set(it.getId());
            });
    }
}
