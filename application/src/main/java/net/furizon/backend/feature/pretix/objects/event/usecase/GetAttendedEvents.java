package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.dto.GetEventsResponse;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAttendedEvents implements UseCase<GetAttendedEvents.Input, GetEventsResponse> {
    @NotNull
    private final EventFinder eventFinder;
    @NotNull
    private final GeneralChecks generalChecks;

    @Override
    public @NotNull GetEventsResponse executor(@NotNull GetAttendedEvents.Input input) {
        long userId = generalChecks.getUserIdAndAssertPermission(
                input.userId,
                input.user,
                Permission.CAN_MANAGE_USER_PUBLIC_INFO
        );
        log.info("User {} is checking attended events of user {}", input.user.getUserId(), userId);
        return new GetEventsResponse(
                eventFinder.getAttendedEvents(userId)
        );
    }

    public record Input(
            @Nullable Long userId,
            @NotNull FurizonUser user
    ) {}
}
