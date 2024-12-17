package net.furizon.backend.feature.room.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomDataResponse;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface RoomFinder {

    boolean isUserInAroom(long userId, long eventId);
    boolean isUserInvitedInRoom(long userId, long roomId);
    boolean userOwnsAroom(long userId, long eventId);

    @Nullable
    RoomInfo getRoomInfoForUser(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation);

    RoomDataResponse getRoomDataForUser(
            long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation);

    @NotNull
    List<RoomGuestResponse> getRoomGuestsFromRoomId(long roomId, boolean onlyConfirmed);

    @NotNull
    Optional<Long> getRoomIdFromOwnerUserId(long userId, @NotNull Event event);

    @NotNull
    Optional<Long> getOwnerUserIdFromRoomId(long roomId);

    @NotNull
    List<RoomGuestResponse> getUserReceivedInvitations(long userId, @NotNull Event event);

    @NotNull
    Optional<Boolean> isRoomConfirmed(long roomId);

    @NotNull
    Optional<RoomGuestResponse> getConfirmedRoomGuestFromUserEvent(long userId, @NotNull Event event);

    @NotNull
    Optional<RoomGuestResponse> getRoomGuestFromId(long roomGuestId);

    @NotNull
    Optional<Short> getRoomCapacity(long roomId);
}
