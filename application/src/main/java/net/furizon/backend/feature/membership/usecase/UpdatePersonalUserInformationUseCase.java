package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.updateMembershipInfo.UpdateMembershipInfoAction;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePersonalUserInformationUseCase implements
        UseCase<UpdatePersonalUserInformationUseCase.Input, Boolean> {
    @NotNull private final UpdateMembershipInfoAction updateMembershipInfoAction;

    @Override
    public @NotNull Boolean executor(@NotNull UpdatePersonalUserInformationUseCase.Input input) {
        log.info("User {} is updating his personal infos", input.userId);
        updateMembershipInfoAction.invoke(input.userId, input.info, input.event);
        return true;
    }

    public record Input(long userId, @NotNull PersonalUserInformation info, @NotNull Event event){}
}
