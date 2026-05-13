package net.furizon.backend.feature.pretix.objects.checkins.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinHistory;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.CheckinType;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.PretixCheckinHistory;
import net.furizon.backend.feature.pretix.objects.checkins.dto.request.CheckinHistoryOrder;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.CheckinHistoryResponse;
import net.furizon.backend.feature.pretix.objects.checkins.finder.PretixCheckinHistoryFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchCheckinLogsUseCase implements UseCase<FetchCheckinLogsUseCase.Input, CheckinHistoryResponse> {
    @NotNull
    private final PretixCheckinHistoryFinder historyFinder;

    @NotNull
    private final PersonalInfoFinder personalInfoFinder;

    @NotNull
    private final OrderFinder orderFinder;

    @NotNull
    private final UserFinder userFinder;

    @NotNull
    private final TranslationService translationService;

    @Override
    public @NotNull CheckinHistoryResponse executor(@NotNull FetchCheckinLogsUseCase.Input input) {
        log.info("User {} is fetching checkin logs", input.user.getUserId());

        var p = input.event.getOrganizerAndEventPair();

        PretixPaging<PretixCheckinHistory> history = historyFinder.getPagedCheckinLists(
                p.getOrganizer(), p.getEvent(),

                input.createdSince,
                input.createdBefore,
                input.datetimeSince,
                input.datetimeBefore,
                input.successful,
                input.checkinListId,
                input.type,
                input.autoCheckedIn,
                input.order,

                input.page
        );

        var results = Optional.ofNullable(history.getResults()).orElse(Collections.emptyList());

        Map<Long, Order> orders = orderFinder.findOrdersByTicketPositionIds(
            history.getResults().stream().map(PretixCheckinHistory::getPositionId).filter(Objects::nonNull).toList(),
            input.event,
            input.pretixInformation
        );
        List<Long> userIds = orders.values().stream().map(Order::getOrderOwnerUserId).filter(Objects::nonNull).toList();
        Map<Long, UserDisplayData> users = userFinder.getDisplayUserByIds(userIds, input.event);
        Map<Long, Pair<String, String>> userNames = personalInfoFinder.getFullNameByUserIds(userIds);

        return new CheckinHistoryResponse(
                history.getCount(),
                history.nextPage(false),
                history.previousPage(false),
                results.stream().map(
                        c -> {
                            var b = CheckinHistory.builder()
                                    .checkinId(c.getId())
                                    .successful(c.getSuccessful())
                                    .localizedErrorReason(
                                            Optional.ofNullable(c.getErrorReason())
                                                    .map(r -> r.getErrorMessage().localizeError(translationService))
                                                    .orElse(null)
                                    )
                                    .optionalErrorMessage(c.getErrorExplanation())
                                    .datetime(c.getDatetime())
                                    .createdAt(c.getCreatedAt())
                                    .checkinListId(c.getCheckinListId())
                                    .autoCheckedIn(c.getAutoCheckedIn())
                                    .type(c.getType());
                            Order o = orders.get(c.getPositionId());
                            if (o != null) {
                                b.orderCode(o.getCode());

                                Long userId = o.getOrderOwnerUserId();
                                if (userId != null) {
                                    UserDisplayData u = users.get(userId);
                                    if (u != null) {
                                        b.user(u);
                                    }
                                    Pair<String, String> name = userNames.get(userId);
                                    if (name != null) {
                                        b.firstName(name.getLeft());
                                        b.lastName(name.getRight());
                                    }
                                }
                            }
                            return b.build();
                        }
                    ).toList()
        );
    }

    public record Input(
            @Nullable OffsetDateTime createdSince,
            @Nullable OffsetDateTime createdBefore,
            @Nullable OffsetDateTime datetimeSince,
            @Nullable OffsetDateTime datetimeBefore,
            @Nullable Boolean successful,
            @Nullable Long checkinListId,
            @Nullable CheckinType type,
            @Nullable Boolean autoCheckedIn,
            @Nullable CheckinHistoryOrder order,

            @Nullable Integer page,

            @NotNull Event event,
            @NotNull PretixInformation pretixInformation,
            @NotNull FurizonUser user
    ) {}
}
