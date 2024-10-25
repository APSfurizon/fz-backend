package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.PretixAnswer;
import net.furizon.backend.feature.pretix.order.PretixOrder;
import net.furizon.backend.feature.pretix.order.PretixPosition;
import net.furizon.backend.feature.pretix.product.PretixProductResults;
import net.furizon.backend.feature.pretix.product.usecase.ReloadProductsUseCase;
import net.furizon.backend.feature.pretix.question.PretixQuestion;
import net.furizon.backend.feature.pretix.question.usecase.ReloadQuestionsUseCase;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.model.*;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import net.furizon.backend.utils.pretix.Constants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.furizon.backend.infrastructure.pretix.Const.QUESTIONS_ACCOUNT_SECRET;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedPretixInformation implements PretixInformation {
    @NotNull private final UseCaseExecutor useCaseExecutor;

    @NotNull private final UserFinder userFinder;

    // *** CACHE

    @NotNull private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);

    //Event Struct

    //Contains tickets, memberships, sponsors, rooms
    @NotNull private final Cache<CacheItemTypes, Set<Integer>> itemIdsCache = Caffeine.newBuilder().build();

    //Questions
    @NotNull private final AtomicReference<Integer> questionSecretId = new AtomicReference<>(-1);
    @NotNull private final Cache<Integer, QuestionType> questionIdToType = Caffeine.newBuilder().build();
    @NotNull private final Cache<Integer, String> questionIdToIdentifier = Caffeine.newBuilder().build();
    @NotNull private final Cache<String, Integer> questionIdentifierToId = Caffeine.newBuilder().build();

    //Tickets
    @NotNull private final Cache<Integer, Integer> dailyIdToDay = Caffeine.newBuilder().build(); //map id -> day idx

    //Sponsors
    @NotNull private final Cache<Integer, Sponsorship> sponsorshipIdToType = Caffeine.newBuilder().build();

    //Extra days
    @NotNull private final Cache<Integer, ExtraDays> extraDaysIdToDay = Caffeine.newBuilder().build();

    //Rooms
    //map id -> (capacity, hotelName)
    @NotNull private final Cache<Integer, Pair<Short, String>> roomIdToInfo = Caffeine.newBuilder().build();
    //map capacity/name -> room name TODO CHECK IF THIS WORKS
    @NotNull private final Cache<Pair<Short, String>, String> roomInfoToName = Caffeine.newBuilder().build();

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
    public Optional<QuestionType> getQuestionTypeFromId(int id) {
        return Optional.ofNullable(questionIdToType.getIfPresent(id));
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier) {
        return Optional.ofNullable(
                questionIdToType.getIfPresent(questionIdentifierToId.getIfPresent(identifier))
        );
    }

    @NotNull
    @Override
    public Optional<String> getQuestionIdentifierFromId(int id) {
        return Optional.ofNullable(questionIdToIdentifier.getIfPresent(id));
    }

    @NotNull
    @Override
    public Optional<Integer> getQuestionIdFromIdentifier(@NotNull String identifier) {
        return Optional.ofNullable(questionIdentifierToId.getIfPresent(identifier));
    }

    @NotNull
    @Override
    public Optional<Order> parseOrderFromId(@NotNull PretixOrder pretixOrder, @NotNull Event event) {
        Integer cacheDay;
        ExtraDays cacheExtraDays;
        BiFunction<CacheItemTypes, @NotNull Integer, @NotNull Boolean> checkItemId = (type, item) ->
            Objects.requireNonNull(itemIdsCache.getIfPresent(type)).contains(item);

        boolean hasTicket = false; //If no ticket is found, we don't store the order at all

        String code = pretixOrder.getCode();
        String secret = pretixOrder.getSecret();
        OrderStatus status = OrderStatus.get(pretixOrder.getStatus());

        Set<Integer> days = new HashSet<>();
        Sponsorship sponsorship = Sponsorship.NONE;
        ExtraDays extraDays = ExtraDays.NONE;
        List<PretixAnswer> answers = null;
        int answersMainPositionId = 0;
        String hotelLocation = null;
        boolean membership = false;
        String userSecret = null;
        short roomCapacity = 0;

        List<PretixPosition> positions = pretixOrder.getPositions();
        if (positions.isEmpty()) {
            status = OrderStatus.CANCELED;
        }

        for (PretixPosition position : positions) {
            int item = position.getItemId();

            if (checkItemId.apply(CacheItemTypes.TICKETS, item)) {
                hasTicket = true;
                answersMainPositionId = position.getPositionId();
                answers = position.getAnswers();
                for (PretixAnswer answer : answers) {
                    int questionId = answer.getQuestionId();
                    var questionType = getQuestionTypeFromId(questionId);
                    if (questionType.isPresent()) {
                        if (questionType.get() == QuestionType.FILE) {
                            answer.setAnswer(Const.QUESTIONS_FILE_KEEP);
                        }
                        if (questionId == this.getQuestionSecretId()) {
                            userSecret = answer.getAnswer();
                        }
                    }
                }

            } else if ((cacheDay = dailyIdToDay.getIfPresent(item)) != null)  {
                days.add(cacheDay);

            } else if (checkItemId.apply(CacheItemTypes.MEMBERSHIP_CARDS, item)) {
                membership = true;

            } else if (checkItemId.apply(CacheItemTypes.SPONSORSHIPS, item)) {
                Sponsorship s = sponsorshipIdToType.getIfPresent(position.getVariationId());
                if (s != null && s.ordinal() > sponsorship.ordinal()) {
                    sponsorship = s; //keep the best sponsorship
                }

            } else if ((cacheExtraDays = extraDaysIdToDay.getIfPresent(item)) != null)  {
                if (extraDays != ExtraDays.BOTH) {
                    if (extraDays != cacheExtraDays && extraDays != ExtraDays.NONE) {
                        extraDays = ExtraDays.BOTH;
                    } else {
                        extraDays = cacheExtraDays;
                    }
                }

            } else if (checkItemId.apply(CacheItemTypes.ROOMS, item)) {
                Pair<Short, String> room = roomIdToInfo.getIfPresent(position.getVariationId());
                if (room != null) {
                    roomCapacity = room.getFirst();
                    hotelLocation = room.getSecond();
                }
            }
        }

        if (hasTicket) {
            Order order = Order.builder()
                    .code(code)
                    .orderStatus(status)
                    .sponsorship(sponsorship)
                    .extraDays(extraDays)
                    .dailyDays(days)
                    .roomCapacity(roomCapacity)
                    .hotelLocation(hotelLocation)
                    .pretixOrderSecret(secret)
                    .hasMembership(membership)
                    .answersMainPositionId(answersMainPositionId)
                    .orderOwner(userFinder.findBySecret(userSecret))
                    .orderEvent(event)
                    .answers(answers, this)
                .build();
            return Optional.of(order);
        }
        return Optional.empty();
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
        log.info("[PRETIX] Resetting event cache");
        currentEvent.set(null);
    }

    private void invalidateEventStructCache() {
        log.info("[PRETIX] Resetting event's struct cache");

        //General
        itemIdsCache.invalidateAll();

        //Questions
        questionSecretId.set(-1);
        questionIdToType.invalidateAll();
        questionIdToIdentifier.invalidateAll();
        questionIdentifierToId.invalidateAll();

        //Tickets
        dailyIdToDay.invalidateAll();

        //Sponsors
        sponsorshipIdToType.invalidateAll();

        //Extra days
        extraDaysIdToDay.invalidateAll();

        //Rooms
        roomIdToInfo.invalidateAll();
        roomInfoToName.invalidateAll();
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
        reloadProducts(event);
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

    private void reloadProducts(Event event) {
        PretixProductResults products = useCaseExecutor.execute(ReloadProductsUseCase.class, event);

        itemIdsCache.put(CacheItemTypes.TICKETS, products.ticketItemIds());
        itemIdsCache.put(CacheItemTypes.MEMBERSHIP_CARDS, products.membershipCardItemIds());
        itemIdsCache.put(CacheItemTypes.SPONSORSHIPS, products.sponsorshipItemIds());
        itemIdsCache.put(CacheItemTypes.ROOMS, products.roomItemIds());

        dailyIdToDay.putAll(products.dailyIdToDay());
        sponsorshipIdToType.putAll(products.sponsorshipIdToType());
        extraDaysIdToDay.putAll(products.extraDaysIdToDay());
        roomIdToInfo.putAll(products.roomIdToInfo());
        roomInfoToName.putAll(products.roomInfoToName());
    }
}
