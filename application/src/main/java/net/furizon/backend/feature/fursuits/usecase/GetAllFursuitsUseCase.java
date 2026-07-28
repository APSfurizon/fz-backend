package net.furizon.backend.feature.fursuits.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.dto.FursuitListResponse;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllFursuitsUseCase implements UseCase<GetAllFursuitsUseCase.Input, FursuitListResponse> {
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final OrderFinder orderFinder;

    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final FursuitConfig fursuitConfig;

    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitChecks fursuitChecks;

    @Override
    public @NotNull FursuitListResponse executor(@NotNull GetAllFursuitsUseCase.Input input) {
        Event event = input.pretixInformation.getCurrentEvent();
        boolean isAdmin = fursuitChecks.isAdmin(input.user.getUserId());
        long userId = generalChecks.getUserIdAndAssertPermission(input.userId, input.user, null, isAdmin);

        return invoke(userId, event, input.pretixInformation);
    }

    public @NotNull FursuitListResponse invoke(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        Order order = orderFinder.findOrderByUserIdEvent(userId, event, pretixInformation);
        int maxFursuits = order == null ? 0 : fursuitConfig.getDefaultFursuitsNo() + order.getExtraFursuits();
        List<FursuitData> fursuits = fursuitFinder.getFursuitsOfUser(userId, event);
        long bringingToEvent = fursuits.stream().filter(FursuitData::isBringingToEvent).count();

        boolean canBringFursuitToEvent = order != null
                && order.getOrderStatus() == OrderStatus.PAID
                && bringingToEvent < maxFursuits;

        boolean allowEditBringFursuitToEvent = generalChecks.isTimeframeForEventOk(
                badgeConfig.getEditingDeadline(), null);

        return new FursuitListResponse(
                fursuits,
                (short) bringingToEvent,
                (short) maxFursuits,
                canBringFursuitToEvent,
                allowEditBringFursuitToEvent
        );
    }

    public static GetAllFursuitsUseCase INSTANCE = null;
    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable Long userId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
