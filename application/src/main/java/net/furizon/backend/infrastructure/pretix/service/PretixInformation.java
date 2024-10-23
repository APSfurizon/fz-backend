package net.furizon.backend.infrastructure.pretix.service;

import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixInformation {
    @NotNull
    Optional<Event> getCurrentEvent();

    int getQuestionSecretId();

    @NotNull
    Optional<QuestionType> getQuestionTypeById(int id);

    void resetCache();
}
