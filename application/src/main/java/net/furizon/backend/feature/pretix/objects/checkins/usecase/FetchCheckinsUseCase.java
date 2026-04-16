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
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

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

        PretixPaging<PretixPosition> res = search.getPagedCheckinSearchResults(
                input.event.getOrganizerAndEventPair().getOrganizer(),

                input.search,
                input.checkinListId,
                input.hasCheckedIn,
                input.order,

                input.page
        );

        return new CheckinSearchResponse(
                res.getCount(),
                res.nextPage(),
                res.previousPage(),
                Optional.ofNullable(res.getResults()).orElse(Collections.emptyList()).stream().map(
                        r -> {
                            String orderCode = r.getOriginalOrderCode();
                            Order o = orderFinder.findOrderByCodeEvent(
                                orderCode,
                                input.event.getId(),
                                input.pretixInformation
                            );
                            if (o == null) {
                                log.warn("Order {} not found", orderCode);
                                return null;
                            }
                            Long userId = o.getOrderOwnerUserId();
                            if (userId == null) {
                                log.warn("Order {} has no user associated", orderCode);
                                return null;
                            }
                            UserDisplayData user = userFinder.getDisplayUser(userId, input.event);
                            if (user == null) {
                                log.warn("User {} not found", userId);
                                return null;
                            }

                            String name = r.getName();
                            if (name == null) {
                                var pui = personalInfoFinder.findByUserId(userId);
                                if (pui == null) {
                                    log.warn("User {} has no personal info", userId);
                                    return null;
                                }
                                name = String.format("%s %s", pui.getFirstName(), pui.getLastName());
                            }

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
            @Nullable CheckinSearchOrder order,

            @Nullable Integer page,

            @NotNull Event event,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ) {}
}
