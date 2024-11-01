package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.action.addAnswer.AddPretixAnswerAction;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushAnswersToPretixUseCase implements UseCase<PushAnswersToPretixUseCase.Input, Boolean> {
    @NotNull
    private final AddPretixAnswerAction addPretixAnswerAction;

    @NotNull
    @Override
    public Boolean executor(@NotNull PushAnswersToPretixUseCase.Input input) {
        return addPretixAnswerAction.invoke(
            input.order,
            input.event.getOrganizerAndEventPair()
        );
    }

    public record Input(
        @NotNull Event event,
        @NotNull Order order,
        @NotNull PretixInformation pretixInformation
    ) {}
}
