package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.FursuitErrorCodes;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSingleFursuitUseCase implements UseCase<GetSingleFursuitUseCase.Input, FursuitData> {
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final FursuitChecks checks;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull FursuitData executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        Event event = input.event;

        FursuitData fursuit = fursuitFinder.getFursuit(userId, event, false);
        if (fursuit == null) {
            throw new ApiException(translationService.error("badge.fursuit.not_found"),
                FursuitErrorCodes.FURSUIT_NOT_FOUND);
        }

        //We assume that if someone is bringing his fursuit to an event, the info
        // of the fursuit are already public
        if (!fursuit.isBringingToEvent()) {
            checks.assertUserHasPermissionOnFursuit(userId, fursuit);
        }

        return fursuit;
    }

    public record Input(
            long fursuitId,
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
