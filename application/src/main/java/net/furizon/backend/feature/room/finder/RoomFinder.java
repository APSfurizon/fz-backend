package net.furizon.backend.feature.room.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RoomFinder {

    @Nullable
    RoomInfo getRoomInfoForUser(long userId, @NotNull Event event);

    @NotNull
    List<RoomGuestResponse> getRoomGuests(long roomId);

    @NotNull
    List<RoomGuestResponse> getUserReceivedInvitations(long userId, @NotNull Event event);
}
