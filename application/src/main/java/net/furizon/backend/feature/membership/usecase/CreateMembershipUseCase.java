package net.furizon.backend.feature.membership.usecase;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.createMembershipCard.CreateMembershipCardAction;
import net.furizon.backend.feature.membership.dto.AddMembershipCardRequest;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateMembershipUseCase implements UseCase<CreateMembershipUseCase.Input, Boolean> {
    @NotNull private final CreateMembershipCardAction createMembershipCardAction;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        log.info(
                "Admin {} is manually creating membership card for user {} ",
                input.user.getUserId(), input.req.getUserId()
        );
        createMembershipCardAction.invoke(input.req.getUserId(), input.event);
        return true;
    }

    public record Input(@NotNull AddMembershipCardRequest req, @NotNull FurizonUser user, @Nullable Event event) {}
}
