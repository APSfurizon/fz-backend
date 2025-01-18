package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetFullInfoBadgeUseCase implements UseCase<GetFullInfoBadgeUseCase.Input, FullInfoBadgeResponse> {
    @NotNull private final FursuitConfig fursuitConfig;
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull FullInfoBadgeResponse executor(@NotNull GetFullInfoBadgeUseCase.Input input) {
        Event event = input.event;
        FurizonUser user = input.user;
        long userId = user.getUserId();

        UserDisplayData userData = userFinder.getDisplayUser(userId, event);
        OffsetDateTime editingDeadline = badgeConfig.getEditingDeadline();

        Short orderFursuits = orderFinder.getBoughtExtraFursuits(userId, event);
        short maxFursuits = (short) (orderFursuits == null ? 0 : fursuitConfig.getDefaultFursuitsNo() + orderFursuits);
        List<FursuitDisplayData> fursuits = fursuitFinder.getFursuitsOfUser(userId, event);

        return new FullInfoBadgeResponse(
                userData,
                editingDeadline,
                fursuits,
                maxFursuits
        );
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
