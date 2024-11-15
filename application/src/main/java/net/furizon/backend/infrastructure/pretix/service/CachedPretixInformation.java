package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.event.usecase.ReloadEventsUseCase;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.usecase.ReloadOrdersUseCase;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProductResults;
import net.furizon.backend.feature.pretix.objects.product.usecase.ReloadProductsUseCase;
import net.furizon.backend.feature.pretix.objects.question.PretixQuestion;
import net.furizon.backend.feature.pretix.objects.question.usecase.ReloadQuestionsUseCase;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCaseInput;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

import static net.furizon.backend.infrastructure.pretix.Const.QUESTIONS_ACCOUNT_SECRET;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedPretixInformation implements PretixInformation {
    @NotNull
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    @NotNull
    private final UseCaseExecutor useCaseExecutor;
    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final EventFinder eventFinder;
    @NotNull
    private final PretixConfig pretixConfig;

    // *** CACHE
    @NotNull
    private final AtomicReference<Event> currentEvent = new AtomicReference<>(null);

    //Event Struct

    //Contains tickets, memberships, sponsors, rooms
    @NotNull
    private final Cache<CacheItemTypes, Set<Integer>> itemIdsCache = Caffeine.newBuilder().build();

    //Questions
    @NotNull
    private final AtomicReference<Integer> questionSecretId = new AtomicReference<>(-1);
    @NotNull
    private final Cache<Integer, QuestionType> questionIdToType = Caffeine.newBuilder().build();
    @NotNull
    private final Cache<Integer, String> questionIdToIdentifier = Caffeine.newBuilder().build();
    @NotNull
    private final Cache<String, Integer> questionIdentifierToId = Caffeine.newBuilder().build();

    //Tickets
    @NotNull
    private final Cache<Integer, Integer> dailyIdToDay = Caffeine.newBuilder().build(); //map id -> day idx

    //Sponsors
    @NotNull
    private final Cache<Integer, Sponsorship> sponsorshipIdToType = Caffeine.newBuilder().build();

    //Extra days
    @NotNull
    private final Cache<Integer, ExtraDays> extraDaysIdToDay = Caffeine.newBuilder().build();

    //Rooms
    //map id -> (capacity, hotelName)
    @NotNull
    private final Cache<Integer, HotelCapacityPair> roomIdToInfo = Caffeine.newBuilder().build();
    //map capacity/name -> room name
    @NotNull
    private final Cache<HotelCapacityPair, Map<String, String>> roomInfoToNames =
        Caffeine.newBuilder().build();

    @PostConstruct
    public void init() {
        if (!pretixConfig.isEnableSync()) {
            log.warn("[PRETIX] Pretix initialization has been disabled");
            return;
        }
        log.info("[PRETIX] Initializing pretix information and cache it");
        resetCache();
        reloadAllOrders();
    }

    @NotNull
    @Override
    public Optional<Event> getCurrentEvent() {
        lock.readLock().lock();
        var v = currentEvent.get();
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @Override
    public int getQuestionSecretId() {
        lock.readLock().lock();
        var v = questionSecretId.get();
        lock.readLock().unlock();
        return v;
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromId(int id) {
        lock.readLock().lock();
        var v = questionIdToType.getIfPresent(id);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<QuestionType> getQuestionTypeFromIdentifier(@NotNull String identifier) {
        lock.readLock().lock();
        var v = questionIdToType.getIfPresent(questionIdentifierToId.getIfPresent(identifier));
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<String> getQuestionIdentifierFromId(int id) {
        lock.readLock().lock();
        var v = questionIdToIdentifier.getIfPresent(id);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<Integer> getQuestionIdFromIdentifier(@NotNull String identifier) {
        lock.readLock().lock();
        var v = questionIdentifierToId.getIfPresent(identifier);
        lock.readLock().unlock();
        return Optional.ofNullable(v);
    }

    @NotNull
    @Override
    public Optional<Order> parseOrderFromId(@NotNull PretixOrder pretixOrder, @NotNull Event event) {
        lock.readLock().lock();
        try {
            Integer cacheDay;
            ExtraDays cacheExtraDays;
            BiFunction<CacheItemTypes, @NotNull Integer, @NotNull Boolean> checkItemId = (type, item) ->
                    Objects.requireNonNull(itemIdsCache.getIfPresent(type)).contains(item);

            boolean hasTicket = false; //If no ticket is found, we don't store the order at all
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

                } else if ((cacheDay = dailyIdToDay.getIfPresent(item)) != null) {
                    days.add(cacheDay);

                } else if (checkItemId.apply(CacheItemTypes.MEMBERSHIP_CARDS, item)) {
                    membership = true;

                } else if (checkItemId.apply(CacheItemTypes.SPONSORSHIPS, item)) {
                    Sponsorship s = sponsorshipIdToType.getIfPresent(position.getVariationId());
                    if (s != null && s.ordinal() > sponsorship.ordinal()) {
                        sponsorship = s; //keep the best sponsorship
                    }

                } else if ((cacheExtraDays = extraDaysIdToDay.getIfPresent(item)) != null) {
                    if (extraDays != ExtraDays.BOTH) {
                        if (extraDays != cacheExtraDays && extraDays != ExtraDays.NONE) {
                            extraDays = ExtraDays.BOTH;
                        } else {
                            extraDays = cacheExtraDays;
                        }
                    }

                } else if (checkItemId.apply(CacheItemTypes.ROOMS, item)) {
                    HotelCapacityPair room = roomIdToInfo.getIfPresent(position.getVariationId());
                    if (room != null) {
                        roomCapacity = room.capacity();
                        hotelLocation = room.hotel();
                    }
                }
            }

            if (!hasTicket) {
                return Optional.empty();
            }

            return Optional.of(
                Order.builder()
                    .code(pretixOrder.getCode())
                    .orderStatus(status)
                    .sponsorship(sponsorship)
                    .extraDays(extraDays)
                    .dailyDays(days)
                    .roomCapacity(roomCapacity)
                    .hotelLocation(hotelLocation)
                    .pretixOrderSecret(pretixOrder.getSecret())
                    .hasMembership(membership)
                    .answersMainPositionId(answersMainPositionId)
                    .eventId(event.getId())
                    //TODO add orderOwenrId
                    .answers(answers, this)
                    .userFinder(userFinder)
                    .eventFinder(eventFinder)
                    .build()
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void resetCache() {
        log.info("[PRETIX] Resetting cache for pretix information");

        lock.writeLock().lock();
        try {
            invalidateEventsCache();
            invalidateEventStructCache();

            // reloading events
            reloadEvents();
            reloadEventStructure();
        } finally {
            lock.writeLock().unlock();
        }
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
        roomInfoToNames.invalidateAll();
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
        Integer accountSecretId = questionIdentifierToId.getIfPresent(QUESTIONS_ACCOUNT_SECRET);
        if (accountSecretId != null) {
            log.info("[PRETIX] Account secret id found, setup it on value = '{}'", accountSecretId);
            questionSecretId.set(accountSecretId);
        }
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
        roomInfoToNames.putAll(products.roomInfoToNames());
    }

    @Override
    public void reloadAllOrders() {
        var event = getCurrentEvent();
        if (event.isPresent()) {
            var input = new ReloadOrdersUseCase.Input(event.get(), this);
            useCaseExecutor.execute(ReloadOrdersUseCase.class, input);
        }
    }

    @Scheduled(cron = "${pretix.cache-reload-cronjob}")
    private void cronReloadCache() {
        log.info("[PRETIX] Cronjob running");
        init();
    }
}
