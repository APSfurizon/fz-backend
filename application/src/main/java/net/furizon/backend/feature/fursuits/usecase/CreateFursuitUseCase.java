package net.furizon.backend.feature.fursuits.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.fursuits.action.createFursuit.CreateFursuitAction;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateFursuitUseCase implements UseCase<CreateFursuitUseCase.Input, FursuitDisplayData> {
    @NotNull private final CreateFursuitAction createFursuitAction;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitChecks fursuitChecks;

    @Override
    public @NotNull FursuitDisplayData executor(@NotNull Input input) {
        long userId = input.user.getUserId();
        log.info("User {} is creating fursuit {}", userId, input.name);

        fursuitChecks.assertUserHaNotReachedMaxBackendFursuitNo(userId);

        Order order = null;
        if (input.bringToCurrentEvenet) {
            order = generalChecks.getOrderAndAssertItExists(
                    userId,
                    input.pretixInformation.getCurrentEvent(),
                    input.pretixInformation
            );

            fursuitChecks.assertUserHanNotReachedMaxFursuitBadges(userId, order);
        }

        long fursuitId = createFursuitAction.invoke(
                userId,
                input.name,
                input.species,
                order
        );

        return FursuitDisplayData.builder()
                .id(fursuitId)
                .name(input.name)
                .species(input.species)
                .bringingToEvent(input.bringToCurrentEvenet)
                .ownerId(userId)
            .build();
    }

    public record Input(
            @NotNull String name,
            @NotNull String species,
            boolean bringToCurrentEvenet,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixInformation
    ){}
}
