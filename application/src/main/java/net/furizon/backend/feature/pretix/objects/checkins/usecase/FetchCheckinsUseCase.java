package net.furizon.backend.feature.pretix.objects.checkins.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinSearchResult;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinSearchOrder;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.CheckinSearchResponse;
import net.furizon.backend.feature.pretix.objects.checkins.finder.PretixCheckinSearchFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.feature.user.objects.SearchUserResult;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchCheckinsUseCase implements UseCase<FetchCheckinsUseCase.Input, CheckinSearchResponse> {

    @NotNull
    private final PretixCheckinSearchFinder search;

    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final OrderFinder orderFinder;
    @NotNull
    private final PersonalInfoFinder personalInfoFinder;

    @Override
    public @NotNull CheckinSearchResponse executor(@NotNull FetchCheckinsUseCase.Input input) {
        log.info("User {} is searching for checkins", input.user.getUserId());
        final String organizer = input.event.getOrganizerAndEventPair().getOrganizer();

        PretixPaging<PretixPosition> res = search.getPagedCheckinSearchResults(
                organizer,

                input.search,
                input.checkinListId,
                input.hasCheckedIn,
                input.searchOrder,

                input.page
        );
        var results = Optional.ofNullable(res.getResults()).orElse(Collections.emptyList());
        int resCount = res.getCount();
        Integer nextPage = res.nextPage(false);
        Integer prevPage = res.previousPage(false);

        if (results.isEmpty()) {
            if (input.search == null) {
                return new CheckinSearchResponse(
                        res.getCount(),
                        res.nextPage(false),
                        res.previousPage(false),
                        Collections.emptyList()
                );
            }
            List<SearchUserResult> internalSearch = userFinder.searchUserInCurrentEvent(
                    input.search,
                    true,
                    input.event,
                    false, false, false,
                    null, null, null
            );

            Map<Long, Order> internalSearchOrders = orderFinder.findOrderByUserIdsEvent(
                    internalSearch.stream().map(SearchUserResult::id).toList(),
                    input.event,
                    input.pretixInformation
            );

            results = new ArrayList<>(internalSearch.size());
            Set<Long> positionsAlreadyFound = new HashSet<>();
            for (SearchUserResult r : internalSearch) {
                Order o = internalSearchOrders.get(r.id());
                if (o == null) {
                    continue;
                }
                PretixPaging<PretixPosition> internalSearchRes = search.getPagedCheckinSearchResults(
                        organizer,

                        o.getCode(),
                        input.checkinListId,
                        input.hasCheckedIn,
                        input.searchOrder,

                        null
                );
                if (internalSearchRes.getResults() == null) {
                    continue;
                }
                for (PretixPosition p : internalSearchRes.getResults()) {
                    long positionId = p.getPositionId();
                    if (!positionsAlreadyFound.contains(positionId)) {
                        positionsAlreadyFound.add(positionId);
                        results.add(p);
                    }
                }
            }
            nextPage = null;
            prevPage = null;
            resCount = positionsAlreadyFound.size();
        }


        Map<String, Order> orders = orderFinder.findOrderByCodesEvent(
                results.stream().map(PretixPosition::getOriginalOrderCode).toList(),
                input.event.getId(),
                input.pretixInformation
        );
        List<Long> userIds = orders.values().stream().map(Order::getOrderOwnerUserId).filter(Objects::nonNull).toList();
        Map<Long, UserDisplayData> users = userFinder.getDisplayUserByIds(userIds, input.event);
        Map<Long, Pair<String, String>> userNames = personalInfoFinder.getFullNameByUserIds(userIds);

        return new CheckinSearchResponse(
                resCount,
                nextPage,
                prevPage,
                results.stream().map(
                        r -> {
                            String orderCode = r.getOriginalOrderCode();
                            Order o = orders.get(orderCode);
                            if (o == null) {
                                log.warn("Order {} not found", orderCode);
                                return null;
                            }
                            Long userId = o.getOrderOwnerUserId();
                            if (userId == null) {
                                log.warn("Order {} has no user associated", orderCode);
                                return null;
                            }
                            UserDisplayData user = users.get(userId);
                            if (user == null) {
                                log.warn("User {} not found", userId);
                                return null;
                            }

                            Pair<String, String> namePair = userNames.get(userId);
                            if (namePair == null) {
                                log.warn("User {} has no personal info", userId);
                                return null;
                            }
                            String name = String.format("%s %s", namePair.getLeft(), namePair.getRight());

                            return CheckinSearchResult.builder()
                                    .name(name)
                                    .orderCode(orderCode)
                                    .hasCheckedIn(r.isCheckedIn())
                                    .user(user)
                                    //Using the one contained in a position allows for multiple positions to be found
                                    .checkinSecret(r.getSecret())
                                .build();
                        }
                    ).filter(Objects::nonNull).toList()
        );
    }

    public record Input(
            @Nullable String search,
            @Nullable Long checkinListId,
            @Nullable Boolean hasCheckedIn,
            @NotNull CheckinSearchOrder searchOrder,

            @Nullable Integer page,

            @NotNull Event event,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ) {}
}
