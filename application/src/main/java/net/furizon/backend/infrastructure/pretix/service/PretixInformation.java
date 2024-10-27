package net.furizon.backend.infrastructure.pretix.service;

import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.PretixOrder;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PretixInformation {
    @NotNull
    Optional<Event> getCurrentEvent();

    int getQuestionSecretId();

    @NotNull
    Optional<QuestionType> getQuestionTypeFromId(int id);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier);

    @NotNull
    Optional<String> getQuestionIdentifierFromId(int id);

    @NotNull
    Optional<Integer> getQuestionIdFromIdentifier(@NotNull String identifier);

    @NotNull
    Optional<Order> parseOrderFromId(@NotNull PretixOrder pretixOrder, @NotNull Event event);

    void resetCache();

    void reloadAllOrders();
}
