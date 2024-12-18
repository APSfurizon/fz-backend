package net.furizon.backend.feature.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
@AllArgsConstructor
public class RoomGuestResponse {
    @NotNull private final RoomGuest roomGuest;

    @NotNull private final User user;

    @Nullable private final OrderStatus orderStatus;
}
