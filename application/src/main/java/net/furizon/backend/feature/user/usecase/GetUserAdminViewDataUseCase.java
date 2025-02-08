package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.user.UserCodes;
import net.furizon.backend.feature.user.dto.UserAdminViewData;
import net.furizon.backend.feature.user.dto.UserAdminViewDisplay;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUserAdminViewDataUseCase implements UseCase<GetUserAdminViewDataUseCase.Input, UserAdminViewData> {
    @NotNull private final UserFinder userFinder;
    @NotNull private final OrderFinder orderFinder;

    @Override
    public @NotNull UserAdminViewData executor(@NotNull Input input) {
        // Find user data first
        final UserAdminViewDisplay userData = userFinder.getUserAdminViewDisplay(input.userId,
                input.event);
        if (userData == null) {
            throw new ApiException("User not found", UserCodes.USER_NOT_FOUND);
        }
        // Then, get orders grouped by events
        final Map<Long, List<Order>> ordersByEventSlug = Objects.requireNonNull(
                orderFinder.findOrdersByUserId(input.userId, input.pretixInformation))
                .stream()
                .collect(Collectors.groupingBy(Order::getEventId));

        return new UserAdminViewData(userData, ordersByEventSlug);
    }

    public record Input(
            @NotNull Long userId,
            @NotNull Event event,
            @NotNull FurizonUser currentUser,
            PretixInformation pretixInformation
    ) {}
}
