package net.furizon.backend.feature.pretix.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushAnswersToPretixUseCase implements UseCase<PushAnswersToPretixUseCase.Input, Boolean> {
    @NotNull
    private final PushPretixAnswerAction pushPretixAnswerAction;

    @NotNull
    @Override
    public Boolean executor(@NotNull PushAnswersToPretixUseCase.Input input) {
        return pushPretixAnswerAction.invoke(
            input.order,
            input.pretixInformation
        );
    }

    public record Input(
        @NotNull Event event,
        @NotNull Order order,
        @NotNull PretixInformation pretixInformation
    ) {}
}
