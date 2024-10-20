package net.furizon.backend.infrastructure.pretix.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.event.Event;
import net.furizon.backend.feature.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    @PostConstruct
    public void init() {
        log.info("Initializing pretix information and cache it");
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

    @Override
    public void resetCache() {
        log.info("Resetting cache for pretix information");
        useCaseExecutor
            .execute(
                ReloadEventsUseCase.class,
                UseCaseInput.EMPTY
            )
            .ifPresent(event -> {
                log.info("Setting an event as current = {}", event);
                currentEvent.set(event);
            });
    }
}
