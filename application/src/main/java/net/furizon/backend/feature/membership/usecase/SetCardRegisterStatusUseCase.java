package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.setCardRegistrationStatus.SetMembershipCardRegistrationStatus;
import net.furizon.backend.feature.membership.dto.SetMembershipCardRegistrationStatusRequest;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetCardRegisterStatusUseCase implements UseCase<SetMembershipCardRegistrationStatusRequest, Boolean> {
    @NotNull private final SetMembershipCardRegistrationStatus setMembershipCardRegistrationStatus;
    @Override
    public @NotNull Boolean executor(@NotNull SetMembershipCardRegistrationStatusRequest input) {
        log.info("Setting card {} registration status to {}", input.getMembershipCardId(), input.getRegistered());
        setMembershipCardRegistrationStatus.invoke(input.getMembershipCardId(), input.getRegistered());
        return true;
    }
}
