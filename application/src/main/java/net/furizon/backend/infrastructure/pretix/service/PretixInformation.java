package net.furizon.backend.infrastructure.pretix.service;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PretixInformation {
    @NotNull
    Event getCurrentEvent();

    long getQuestionUserId();

    @NotNull
    Map<String, String> getRoomNamesFromRoomPretixItemId(long roomPretixItemId);

    @NotNull
    List<PretixState> getStatesOfCountry(String countryIsoCode);

    @NotNull
    Set<Long> getIdsForItemType(CacheItemTypes type);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromId(long id);

    @NotNull
    Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier);

    @NotNull
    Optional<String> getQuestionIdentifierFromId(long id);

    @NotNull
    Optional<Long> getQuestionIdFromIdentifier(@NotNull String identifier);

    @NotNull
    Optional<Order> parseOrder(@NotNull PretixOrder pretixOrder, @NotNull Event event);

    void resetCache();

    void reloadAllOrders();
}
