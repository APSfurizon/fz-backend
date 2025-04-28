package net.furizon.backend.feature.admin.usecase.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.GenerateBadgeRequest;
import net.furizon.backend.feature.admin.dto.PreviewUserBadgeResponse;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreviewUserBadgesUseCase implements UseCase<PreviewUserBadgesUseCase.Input, PreviewUserBadgeResponse> {
    @NotNull private final BadgeFinder badgeFinder;

    @Override
    public @NotNull PreviewUserBadgeResponse executor(@NotNull Input input) {
        String orderCodes = null;
        String orderSerials = null;
        String userIds = null;
        if (input.request != null) {
            orderCodes = input.request.getOrderCodes();
            orderSerials = input.request.getOrderSerials();
            userIds = input.request.getUserIds();
        }

        return new PreviewUserBadgeResponse(
            badgeFinder.previewUserBadge(
                input.pretixInformation.getCurrentEvent(),
                orderCodes,
                orderSerials,
                userIds
            )
        );
    }


    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation,
            @Nullable GenerateBadgeRequest request
    ) {}
}
