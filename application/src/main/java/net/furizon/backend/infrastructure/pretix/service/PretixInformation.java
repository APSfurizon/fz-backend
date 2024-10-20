package net.furizon.backend.infrastructure.pretix.service;

import net.furizon.backend.feature.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixInformation {
    @NotNull
    Optional<Event> getCurrentEvent();

    int getQuestionSecretId();

    void resetCache();
}
