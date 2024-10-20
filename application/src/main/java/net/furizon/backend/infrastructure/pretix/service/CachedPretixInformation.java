package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.event.Event;
import net.furizon.backend.feature.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.feature.question.usecase.ReloadQuestionsUseCase;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static net.furizon.backend.infrastructure.pretix.Const.QUESTIONS_ACCOUNT_SECRET;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedPretixInformation implements PretixInformation {
    @NotNull
    private final UseCaseExecutor useCaseExecutor;

    @NotNull
    private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);

    @NotNull
    private final AtomicReference<Integer> questionSecretId = new AtomicReference<>(-1);

    @NotNull
    private final Cache<Integer, QuestionType> questionTypeIdsCache = Caffeine.newBuilder().build();

    @NotNull
    private final Cache<Integer, String> questionIdentifiersCache = Caffeine.newBuilder().build();

    @NotNull
    private final Cache<String, Integer> questionIdentifiersToIdCache = Caffeine.newBuilder().build();

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
        return Optional.ofNullable(questionTypeIdsCache.getIfPresent(id));
    }

    @Override
    public void resetCache() {
        log.info("[PRETIX] Resetting cache for pretix information");
        questionSecretId.set(-1);
        currentEvent.set(null);
        questionTypeIdsCache.invalidateAll();
        questionIdentifiersCache.invalidateAll();
        questionIdentifiersToIdCache.invalidateAll();

        // reloading events
        useCaseExecutor
            .execute(
                ReloadEventsUseCase.class,
                UseCaseInput.EMPTY
            )
            .ifPresent(event -> {
                log.info("[PRETIX] Setting an event as current = '{}'", event);
                currentEvent.set(event);
            });

        reloadQuestions();
    }

    private void reloadQuestions() {
        final var event = currentEvent.get();
        if (event == null) {
            log.warn("[PRETIX] Current event is null, skipping question reload");
            return;
        }

        final var questionList = useCaseExecutor.execute(
            ReloadQuestionsUseCase.class,
            event
        );
        questionList.forEach(question -> {
            final var questionId = question.getId();
            final var questionType = question.getType();
            final var questionIdentifier = question.getIdentifier();

            questionTypeIdsCache.put(questionId, questionType);
            questionIdentifiersCache.put(questionId, questionIdentifier);
            questionIdentifiersToIdCache.put(questionIdentifier, questionId);
        });
        // searching QUESTIONS_ACCOUNT_SECRET
        questionList
            .stream()
            .filter(it -> it.getIdentifier().equals(QUESTIONS_ACCOUNT_SECRET))
            .findFirst()
            .ifPresent(it -> {
                log.info("[PRETIX] Account secret id found, setup it on value = '{}'", it.getId());
                questionSecretId.set(it.getId());
            });
    }
}
