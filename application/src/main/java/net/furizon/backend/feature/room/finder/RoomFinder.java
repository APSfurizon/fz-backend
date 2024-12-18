package net.furizon.backend.feature.room.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.dto.response.RoomInvitationResponse;
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

    RoomData getRoomDataForUser(
            long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation);

    @NotNull
    List<RoomGuest> getRoomGuestsFromRoomId(long roomId, boolean onlyConfirmed);

    @NotNull
    List<RoomGuestResponse> getRoomGuestResponseFromRoomId(long roomId, @NotNull Event event);

    @NotNull
    Optional<Long> getRoomIdFromOwnerUserId(long userId, @NotNull Event event);

    @NotNull
    Optional<Long> getOwnerUserIdFromRoomId(long roomId);

    @NotNull
    List<RoomInvitationResponse> getUserReceivedInvitations(
            long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation);

    @NotNull
    Optional<Boolean> isRoomConfirmed(long roomId);

    @NotNull
    Optional<RoomGuest> getConfirmedRoomGuestFromUserEvent(long userId, @NotNull Event event);

    @NotNull
    Optional<RoomGuest> getRoomGuestFromId(long roomGuestId);

    @NotNull
    Optional<Short> getRoomCapacity(long roomId);
}