package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.fursuits.dto.FursuitListResponse;
import net.furizon.backend.feature.fursuits.usecase.GetAllFursuitsUseCase;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetFullInfoBadgeUseCase implements UseCase<GetFullInfoBadgeUseCase.Input, FullInfoBadgeResponse> {
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final UserFinder userFinder;

    @Override
    public @NotNull FullInfoBadgeResponse executor(@NotNull GetFullInfoBadgeUseCase.Input input) {
        Event event = input.pretixInformation.getCurrentEvent();
        long userId = input.userId;

        UserDisplayData userData = generalChecks.assertUserFound(userFinder.getDisplayUser(userId, event));
        OffsetDateTime editingDeadline = badgeConfig.getEditingDeadline();

        boolean allowedModifications = generalChecks.isTimeframeForEventOk(badgeConfig.getEditingDeadline(), event);

        FursuitListResponse fursuits = GetAllFursuitsUseCase.INSTANCE.invoke(userId, event, input.pretixInformation);

        return new FullInfoBadgeResponse(
                userData,
                editingDeadline,
                allowedModifications,
                fursuits
        );
    }

    public record Input(
            long userId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
