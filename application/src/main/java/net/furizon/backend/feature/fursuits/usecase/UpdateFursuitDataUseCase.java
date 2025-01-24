package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.updateFursuit.UpdateFursuitAction;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateFursuitDataUseCase implements UseCase<UpdateFursuitDataUseCase.Input, FursuitData> {
    @NotNull private final UpdateFursuitAction updateFursuitAction;
    @NotNull private final FursuitChecks fursuitChecks;

    @Override
    public @NotNull FursuitData executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        long fursuitId = input.fursuitId;
        log.info("User {} is updating fursuit data {}", userId, fursuitId);

        //This currently doesn't support admins

        FursuitData fursuit = fursuitChecks.getFursuitAndAssertItExists(fursuitId, input.event);
        fursuitChecks.assertUserHasPermissionOnFursuit(userId, fursuit);

        boolean res = updateFursuitAction.invoke(
            fursuitId,
            input.name,
            input.species,
            input.showInFursuitCount
        );

        if (res) {
            FursuitDisplayData f = fursuit.getFursuit();
            f.setName(input.name);
            f.setSpecies(input.species);
            fursuit.setShowInFursuitCount(input.showInFursuitCount);
        }
        return fursuit;
    }

    public record Input(
            long fursuitId,
            @NotNull String name,
            @NotNull String species,
            boolean showInFursuitCount,
            @NotNull Event event,
            @NotNull FurizonUser user
    ) {}
}
