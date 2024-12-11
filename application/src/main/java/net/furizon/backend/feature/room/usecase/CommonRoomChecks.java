package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonRoomChecks {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final OrderFinder orderFinder;

    public void runCommonChecks(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        if (!isDaily.isPresent()) {
            log.error("User is trying to manage a room, but he has no registered order!");
            throw new ApiException("User has no registered order");
        }

        if (isDaily.get()) {
            log.error("User is trying to manage a room, but has a daily ticket!");
            throw new ApiException("User has a daily ticket");
        }
    }

    public void userOwnsAroomCheck(long userId, @NotNull Event event) {
        boolean alreadyOwnsAroom = roomFinder.hasUserAlreadyAroom(userId, event.getId());
        if (alreadyOwnsAroom) {
            log.error("User is trying to register a new room, but already has one!");
            throw new ApiException("User already owns a room");
        }
    }

    public void isUserInARoom(long userId, @NotNull Event event) {
        boolean userInRoom = roomFinder.isUserInAroom(userId, event.getId());
        if (userInRoom) {
            log.error("User is trying to register a new room, but he's already in one!");
            throw new ApiException("User already is in a room");
        }
    }
}
