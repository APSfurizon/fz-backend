package net.furizon.backend.infrastructure.rooms;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.room.logic.DefaultRoomLogic;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.room.logic.UserBuysFullRoom;
import net.furizon.backend.feature.room.logic.UserBuysGenericSpot;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class RoomLogicConfiguration {
    @Value("${room.logic:default}")
    private String activeLogic;

    @NotNull private final UserBuysFullRoom fullRoomLogic;
    @NotNull private final UserBuysGenericSpot genericSpotLogic;
    @NotNull private final DefaultRoomLogic defaultLogic;

    @Bean
    @Primary
    public RoomLogic activeRoomLogic() {
        return switch (activeLogic) {
            case "roomLogic-user-buys-full-room" -> fullRoomLogic;
            case "roomLogic-user-buys-generic-spot" -> genericSpotLogic;
            default -> defaultLogic;
        };
    }
}
